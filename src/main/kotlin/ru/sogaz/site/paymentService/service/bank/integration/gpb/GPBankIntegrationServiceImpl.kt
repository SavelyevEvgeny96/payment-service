package ru.sogaz.site.paymentService.service.bank.integration.gpb

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpPaymentClient
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.data.RefundPayloadDto
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBQRImageRequest
import ru.sogaz.site.paymentService.dto.request.GPBStatusSBPRequest
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.bank.GPBRefundResponseDto
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.HeaderStatusEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.mapper.payment.GPBPaymentRequestMapper
import ru.sogaz.site.paymentService.mapper.payment.RegisterCardMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationServiceImpl
import java.time.LocalDateTime

@Service
class GPBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val gpbSbpPaymentClient: GpbSbpPaymentClient,
    private val gpbCardPaymentClient: GpbCardPaymentClient,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
    private val gpBPaymentRequestMapper: GPBPaymentRequestMapper,
    private val tokenService: TokenService, // ⬅ новый сервис токенов
    private val objectMapper: ObjectMapper,
    private val registerCardMapper: RegisterCardMapper,
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao,
    private val waitingPaymentDao: WaitingPaymentDao,
) : BankIntegrationServiceImpl() {
    companion object {
        private const val SESSION = "Session "
        private const val REFUND_DESCRIPTION = "Отмена банковской транзакции"
        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции:"
        private const val PAYMENT_RECURRENT_FALSE = "Платеж не сформирован для paymentId: %s"
        private const val PAYMENT_RECURRENT_SUCCESS = "Платеж успешно сформирован для paymentId: %s"
    }

    private val logger = loggerFor(javaClass)

    override fun provider(): BankEnum = BankEnum.GPB

    // --------------------------------------------------------------------------------------------
    // CARD PAYMENT
    // --------------------------------------------------------------------------------------------

    @Throws(BankIntegrationException::class, RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        payment
            .let { gpBPaymentRequestMapper.toCardRequest(it) }
            .run(::postForCardPaymentLink)
            .run { payment.fillFromResponse(this) }

    override fun registerCardPaymentRecurrentWithDetails(payment: Payment): PaymentRecurrentRegisterData {
        // 1) Мапим платёж в рекуррентный запрос для банка
        val request = gpBPaymentRequestMapper.toRecurrentRequest(payment)

        // 2) Ветвим логику в зависимости от наличия токена
        val result: PaymentRecurrentRegisterData =
            if (request.token.isBlank()) {
                // 2.1) Токена нет -> платёж даже не пытаемся отправлять в банк
                // 2.1.1) Фиксируем время "старта" и "окончания" как один и тот же момент
                val now = LocalDateTime.now()
                payment.paymentStarted = now
                payment.paymentFinished = now
                // 2.1.2) Ставим статус платежа в FAIL
                payment.state = PaymentStatusEnum.FAIL
                // 2.1.3) Логируем неуспешную попытку рекуррентного платежа
                logger.error(PAYMENT_RECURRENT_FALSE.format(payment.id))
                // 2.1.4) Возвращаем результат без ответа банка
                PaymentRecurrentRegisterData(
                    payment = payment,
                    bankResponse = null,
                )
            } else {
                // 2.2) Токен есть -> идём в банк
                // 2.2.1) Фиксируем время старта платежа
                payment.paymentStarted = LocalDateTime.now()
                // 2.2.2) Делаем запрос в банк на рекуррентную оплату
                val bankResp = postForCardPaymentLinkRecurrent(request)
                // 2.2.3) Фиксируем время окончания платежа (после ответа банка)
                payment.paymentFinished = LocalDateTime.now()
                // 2.2.4) Обновляем статус платежа на основании ответа банка
                payment.changeStatus(bankResp)
                // 2.2.5) Обновляем статус ордера и сохраняем его
                orderDao.save(payment.order.changeStatus(bankResp))
                // 2.2.6) Логируем успех рекуррентного платежа
                logger.info(PAYMENT_RECURRENT_SUCCESS.format(payment.id))
                // 2.2.7) Возвращаем результат вместе с ответом банка
                PaymentRecurrentRegisterData(
                    payment = payment,
                    bankResponse = bankResp,
                )
            }
        // 3) Общий save для платежа
        paymentDao.save(payment)
        // 4)сохраняем в таблицу для фоновой задачи по проверке статусов платежа если статус ошибочный
        waitingPaymentDao.saveWaitingForPayment(payment)
        // 5) Возвращаем собранный результат
        return result
    }

    private fun postForCardPaymentLink(request: GPBPaymentRequest): GazpromCardPaymentResponse =
        gpbCardPaymentClient.startPayment(
            tokenService.takePortalId(request.depersonalization),
            request.token,
            request,
        )

    private fun postForCardPaymentLinkRecurrent(request: GPBPaymentRequest): RegisterCardResponseDto =
        try {
            gpbCardPaymentClient.startPaymentRecurrent(
                tokenService.takePortalId(request.depersonalization),
                request.token,
                request,
            )
        } catch (ex: FeignException) {
            var raw = RegisterCardResponseDto()
            val body = ex.contentUTF8()
            if (body.isNotBlank()) {
                raw = objectMapper.readValue(body, RegisterCardResponseDto::class.java)
            }
            registerCardMapper.mapErrorBody(raw)
        }

    override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse =
        gpbSbpPaymentClient.getQrImage(
            GPBQRImageRequest(payment.qrcId!!),
        )

// --------------------------------------------------------------------------------------------
// SBP PAYMENT
// --------------------------------------------------------------------------------------------

    override fun registerSBPPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams?,
    ): Payment {
        val sbpRequest = gpBPaymentRequestMapper.toSbpRequest(payment, apiConfigProperties)
        val sbpHeaders = sbpHeaders(headersParams)
        val response = gpbSbpPaymentClient.startPayment(sbpHeaders, sbpRequest)
        return payment.fillFromResponse(response)
    }

    private fun sbpHeaders(headersParams: GpbSbpHeadersParams?) =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set(HeaderStatusEnum.PAYMENT_DELAY.value, headersParams?.paymentDelay)
            set(HeaderStatusEnum.PROCESS_PAYMENTS.value, headersParams?.processPayments)
            set(HeaderStatusEnum.PAYMENT_STATUS.value, headersParams?.paymentStatus)
        }

// --------------------------------------------------------------------------------------------
// FILL RESPONSE
// --------------------------------------------------------------------------------------------

    private fun Payment.fillFromResponse(response: GazpromCardPaymentResponse) =
        apply {
            state = PaymentStatusEnum.REG
            paymentPageUrl = response.options.paymentPageUrl
            paymentBankId = response.token
        }

    private fun Payment.changeStatus(response: RegisterCardResponseDto) =
        apply {
            state =
                if (response.result?.status == StatusEnum.SUCCESS.value) {
                    PaymentStatusEnum.REG
                } else {
                    PaymentStatusEnum.FAIL
                }
        }

    private fun Order.changeStatus(response: RegisterCardResponseDto) =
        apply {
            status =
                if (response.result?.status == StatusEnum.SUCCESS.value) {
                    OrderStatus.NEW
                } else {
                    OrderStatus.CANCELED
                }
        }

    private fun Payment.fillFromResponse(response: GazpromSBPPaymentResponse) =
        apply {
            state = PaymentStatusEnum.REG
            qrcId = response.data.qrcId
            paymentPageUrl = response.data.payload
            paymentBankId = response.data.qrcId
        }

// --------------------------------------------------------------------------------------------
// STATUS
// --------------------------------------------------------------------------------------------

    override fun requestPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        try {
            when (paymentBankInfo.type) {
                PaymentTypeEnum.CARD -> requestCardPaymentStatus(paymentBankInfo)
                PaymentTypeEnum.SBP -> requestSBPPaymentStatus(paymentBankInfo)
            }
        } catch (ex: RestClientException) {
            logger.debug("$LOG_GPB_API_ERROR ${paymentBankInfo.paymentBankId}", ex)
            throw InnerException(getTraceId(), "$LOG_GPB_API_ERROR ${paymentBankInfo.paymentBankId}")
        }

    /**
     * Регистрирует возврат средств по платежу через GPB Card Payment.
     *
     * Метод:
     * 1. Определяет portalId по данным деперсонализации платежа
     * 2. Открывает сессию в GPB
     * 3. Инициирует возврат средств
     * 4. Гарантированно закрывает сессию (даже при ошибке выполнения)
     *
     * @param payment объект платежа, по которому выполняется возврат
     * @param dto данные для возврата средств (сумма, описание и пр.)
     *
     * @return ответ банка с результатом операции возврата
     *
     * @throws RuntimeException может пробрасывать исключения клиента GPB
     */
    override fun registerRefundForThePayment(
        payment: Payment,
        dto: RefundPayloadDto,
    ): GPBRefundResponseDto {
        val portalId = tokenService.takePortalId(payment.depersonalization)

        val sessionResponse =
            gpbCardPaymentClient.getSessionId(
                portalId = portalId,
                identifier = apiConfigProperties.identifier,
                password = apiConfigProperties.password,
            )

        val sessionToken = SESSION + sessionResponse.sessionId

        val refundAmount =
            payment.getAmountData().getAmountInPennies().toString()

        return try {
            gpbCardPaymentClient.startRefund(
                portalId,
                payment.paymentBankId,
                sessionToken,
                refundAmount,
                CurrencyEnum.RUB.name,
                REFUND_DESCRIPTION,
            )
        } finally {
            gpbCardPaymentClient.finishSessionId(sessionToken, portalId)
        }
    }

    private fun requestCardPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        convertToBankPaymentDetails(
            gpbCardPaymentClient
                .getPaymentStatus(
                    tokenService.takePortalId(paymentBankInfo.depersonalization),
                    paymentBankInfo.paymentBankId,
                ),
        )

    private fun requestSBPPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        convertToBankPaymentDetails(
            gpbSbpPaymentClient
                .getPaymentStatus(GPBStatusSBPRequest(paymentBankInfo.paymentBankId)),
        )

    private fun convertToBankPaymentDetails(response: GpbCardPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response)

    private fun convertToBankPaymentDetails(response: GpbSbpPaymentStatusResponse): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(response.result.firstOrNull())
}
