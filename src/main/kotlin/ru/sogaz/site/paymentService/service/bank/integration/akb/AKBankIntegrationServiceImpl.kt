package ru.sogaz.site.paymentService.service.bank.integration.akb

import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.data.RefundPayloadDto
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.request.AkbCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.request.OrderDto
import ru.sogaz.site.paymentService.dto.request.PreparePushTranRequest
import ru.sogaz.site.paymentService.dto.request.SetSrcTokenRequest
import ru.sogaz.site.paymentService.dto.response.AkbOrderResponse
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.TypeRidEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationServiceImpl
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.measureTimedValue

@Service
class AKBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val restTemplate: RestTemplate,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
) : BankIntegrationServiceImpl() {
    companion object {
        private const val SET_SRC_TOKEN_SUFFIX = "set-src-token?password="
        private const val PUSH_TRAN_SUFFIX = "prepare-push-tran?password="
        private const val REDIRECT_URL_PARAM_NAME = "afterPayRedirectUrl"
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private const val EMPTY_HPP_URL_RESPONSE = "Пустой HPP URL в ответе Банка Россия"
        private const val EMPTY_ORDER_RESPONSE = "Пустой 'order' в ответе Банка Россия"
        private const val EMPTY_REDIRECT_URL_RESPONSE = "Пустой REDIRECT URL в ответе Банка Россия"
        private const val URL_PARAM_NAME_PASSWORD = "?password="
        private const val CONST_URL_PARAMS_FOR_STATUS_REQUEST =
            "&orderDetailLevel=2&" +
                "tranDetailLevel=2&" +
                "actionDetailLevel=2&" +
                "cofpDetailLevel=2&" +
                "consumerDetailLevel=2&" +
                "consumerTokenDetailLevel=2&" +
                "tokenDetailLevel=2"

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val setSrcTokenRequest = SetSrcTokenRequest(token = mapOf(IPS_RU_PARAM_NAME to true))
    }

    private val logger = loggerFor(javaClass)

    @Throws(RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        payment
            .run(::buildCardRequest)
            .run(::postForCardPaymentData)
            .run { payment.fillFromResponse(this) }

    @Throws(RestClientException::class)
    override fun registerSBPPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams?,
    ): Payment =
        payment
            .run(::buildSBPRequest)
            .run(::postForSBPPaymentData)
            .run { payment.fillFromResponse(this) }
            .also(::setSrcToken)
            .apply { paymentPageUrl = preparePushTran(this) }

    override fun provider(): BankEnum = BankEnum.AKB_RUS

    private fun buildCardRequest(payment: Payment): AkbCardAndSbpPaymentRequest =
        buildAkbRequest(
            payment.id!!,
            payment.getAmountData(),
            payment.v4Description(),
            payment.urlToReturn,
            TypeRidEnum.WITH_3DS,
        )

    private fun postForCardPaymentData(request: AkbCardAndSbpPaymentRequest): AkbOrderResponse =
        postForObject(apiConfigProperties.akbUrl, HttpEntity(request, jsonHeaders))

    private fun buildSBPRequest(payment: Payment): AkbCardAndSbpPaymentRequest =
        buildAkbRequest(
            payment.id!!,
            payment.getAmountData(),
            payment.v4Description(),
            payment.urlToReturn,
            TypeRidEnum.QRC_PAY,
        )

    private fun postForSBPPaymentData(request: AkbCardAndSbpPaymentRequest): AkbOrderResponse =
        postForObject(apiConfigProperties.akbSbpUrl, HttpEntity(request, jsonHeaders))

    /**
     * Установить SRC-токен для конкретного заказа
     */
    private fun setSrcToken(payment: Payment) {
        val url =
            "${apiConfigProperties.akbSbpUrl}/${payment.paymentBankId}/$SET_SRC_TOKEN_SUFFIX${payment.paymentPass}"
        try {
            postForObject<Map<String, Any>>(url, HttpEntity(setSrcTokenRequest, jsonHeaders))
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), "Ошибка при установке SRC-токена: ${ex.message}")
        }
    }

    /**
     * Подготовить push-транзакцию для конкретного заказа
     */
    private fun preparePushTran(payment: Payment): String {
        val url = "${apiConfigProperties.akbSbpUrl}/${payment.paymentBankId}/$PUSH_TRAN_SUFFIX${payment.paymentPass}"
        val returnUrl = payment.urlToReturn?.success() ?: apiConfigProperties.backUrlS
        val body = buildPushTranRequest(returnUrl)
        return try {
            val response = postForObject<PreparePushTranResponse>(url, HttpEntity(body, jsonHeaders))
            response.getQrcPayload(IPS_RU_PARAM_NAME) ?: throw InnerException(
                getTraceId(),
                EMPTY_REDIRECT_URL_RESPONSE,
            )
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), "Ошибка при подготовке push-транзакции: ${ex.message}")
        }
    }

    private fun buildPushTranRequest(redirectUrl: String) =
        PreparePushTranRequest(specificByPm = mapOf(IPS_RU_PARAM_NAME to mapOf(REDIRECT_URL_PARAM_NAME to redirectUrl)))

    private inline fun <reified T> postForObject(
        url: String,
        request: Any? = null,
    ): T = postForObjectWithLogging(url, request)

    private inline fun <reified T> postForObjectWithLogging(
        url: String,
        request: Any? = null,
    ): T {
        logger.debug("Prepare for POST AKB request: $url with body: $request")
        return withMeasureTime { restTemplate.postForObject<T>(url, request) }
            .also { logger.debug("Successfully processing request for AKB with response: $it") }
    }

    private inline fun <reified T> withMeasureTime(block: () -> T): T {
        val (value: T, timeTaken) = measureTimedValue(block)
        logger.debug("${timeTaken.inWholeSeconds} whole seconds taken for AKB request")
        return value
    }

    private fun Payment.fillFromResponse(response: AkbOrderResponse): Payment =
        this.apply {
            state = PaymentStatusEnum.REG
            paymentPageUrl = response.order.hppUrl
            paymentBankId = response.order.id.toString()
            paymentPass = response.order.password
        }

    private fun buildAkbRequest(
        paymentId: UUID,
        amountData: AmountData,
        description: String,
        urlToReturn: UrlToReturn?,
        typeRid: TypeRidEnum,
    ) = OrderDto(
        typeRid = typeRid,
        ridByMerchant = paymentId.toString(),
        amount = amountData.getAmount(),
        currency = amountData.currency,
        hppRedirectUrl = urlToReturn?.success() ?: apiConfigProperties.backUrlS,
        adviceIfaceAddress = urlToReturn?.success() ?: apiConfigProperties.backUrlS,
        description = description,
        descriptionHtml = description,
        expTime = plus15MinutesFromNow().takeIf { typeRid == TypeRidEnum.QRC_PAY },
    ).run(::AkbCardAndSbpPaymentRequest)

    private fun plus15MinutesFromNow() = LocalDateTime.now().plusMinutes(15).format(formatter)

    override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse {
        TODO("Not yet implemented")
    }

    override fun requestPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails {
        val url = formUrl(paymentBankInfo)
        return restTemplate
            .getForObject<PaymentAkbStatusResponse>(url)
            .toBankPaymentDetails()
    }

    override fun registerRefundForThePayment(payment: Payment, dto: RefundPayloadDto) {
        TODO("Not yet implemented")
    }

    override fun registerCardPaymentRecurrentWithDetails(payment: Payment): PaymentRecurrentRegisterData {
        TODO("Not yet implemented")
    }

    private fun formUrl(paymentBankInfo: PaymentBankInfo): String =
        buildString {
            append(apiConfigProperties.akbUrl)
            append("/")
            append(paymentBankInfo.paymentBankId)
            append(URL_PARAM_NAME_PASSWORD + paymentBankInfo.paymentPass)
            append(CONST_URL_PARAMS_FOR_STATUS_REQUEST)
        }

    private fun PaymentAkbStatusResponse.toBankPaymentDetails(): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(order, getCurrentStatus())

    private fun PaymentAkbStatusResponse.getCurrentStatus(): AkbPaymentStatusEnum {
        if (order.status != AkbPaymentStatusEnum.CLOSED) {
            return order.status
        }
        return when (order.prevStatus) {
            AkbPaymentStatusEnum.PREPARING,
            AkbPaymentStatusEnum.WAITPUSHTRAN,
            AkbPaymentStatusEnum.AUTHORIZED,
            -> AkbPaymentStatusEnum.PREPARING

            else -> AkbPaymentStatusEnum.CLOSED
        }
    }
}
