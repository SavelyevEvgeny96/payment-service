package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.http.HttpEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.dto.request.AkbCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.request.OrderDto
import ru.sogaz.site.paymentService.dto.request.PreparePushTranRequest
import ru.sogaz.site.paymentService.dto.request.SetSrcTokenRequest
import ru.sogaz.site.paymentService.dto.response.AkbOrderResponse
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.PreparePushTranResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import java.time.Duration
import java.time.Instant

class AKBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val restTemplate: RestTemplate,
) : BankIntegrationServiceImpl() {
    companion object {
        const val QRC_PAY = "QRC_PAY"
        const val WITH_3DS = "WITH_3DS"
        const val RU = "ru"
        const val SET_SRC_TOKEN_SUFFIX = "set-src-token?password="
        const val PUSH_TRAN_SUFFIX = "prepare-push-tran?password="
        const val ID = "id"
        const val REDIRECT_URL = "afterPayRedirectUrl"
        const val IPS_RU = "ipsRu"
        const val EMPTY_HPP_URL_RESPONSE = "Пустой HPP URL в ответе Банка Россия"
        const val EMPTY_ORDER_RESPONSE = "Пустой 'order' в ответе Банка Россия"
    }

    private val logger = loggerFor(javaClass)

    @Throws(RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        buildCardRequest(payment)
            .run { postForCardPayment(this) }
            .also { it.order ?: throw InnerException(getTraceId(), EMPTY_ORDER_RESPONSE) }
            .run { fillPaymentDataFromResponse(this, payment) }
            .also { validateRegisteredPayment(it) }

    private fun buildCardRequest(payment: Payment): AkbCardAndSbpPaymentRequest =
        generateDescription(payment.order?.premiumAmount, payment.order?.subOrders)
            .run { buildCardOrderDto(this, payment) }
            .run { AkbCardAndSbpPaymentRequest(this) }

    private fun buildCardOrderDto(
        dataDescriptionAndPremiumAmount: DataDescriptionAndPremiumAmount,
        payment: Payment,
    ) = OrderDto(
        typeRid = WITH_3DS,
        amount = dataDescriptionAndPremiumAmount.premiumAmount?.toInt(),
        currency = RUB,
        hppRedirectUrl = payment.urlToReturn.success() ?: apiConfigProperties.backUrlS,
        ridByMerchant = payment.id.toString(),
        adviceIfaceAddress = payment.urlToReturn.success() ?: apiConfigProperties.backUrlS,
        description = dataDescriptionAndPremiumAmount.description,
        descriptionHtml = dataDescriptionAndPremiumAmount.description,
        language = RU,
    )

    @Throws(RestClientException::class)
    override fun registerSBPPayment(payment: Payment): Payment =
        buildPaymentSBPRequest(payment)
            .run { postForSBPPaymentData(this) }
            .also { it.order ?: throw InnerException(getTraceId(), EMPTY_ORDER_RESPONSE) }
            .run { fillPaymentDataFromResponse(this, payment) }
            .also { validateRegisteredPayment(it) }
            .also { setSrcToken(it) }
            .apply { paymentPageUrl = preparePushTran(this) }

    private fun buildPaymentSBPRequest(payment: Payment): AkbCardAndSbpPaymentRequest {
        val expTime = nowPlusFormatted(0, 15)
        return generateDescription(payment.order?.premiumAmount, payment.order?.subOrders)
            .run { buildSbpOrderDto(this, payment, expTime) }
            .run { AkbCardAndSbpPaymentRequest(this) }
    }

    private fun buildSbpOrderDto(
        descriptionAndPremiumAmount: DataDescriptionAndPremiumAmount,
        payment: Payment,
        expTime: String,
    ) = OrderDto(
        typeRid = QRC_PAY,
        amount = descriptionAndPremiumAmount.premiumAmount?.toInt(),
        currency = RUB,
        hppRedirectUrl = payment.urlToReturn.success() ?: apiConfigProperties.backUrlS,
        ridByMerchant = payment.id.toString(),
        adviceIfaceAddress = payment.urlToReturn.success() ?: apiConfigProperties.backUrlS,
        description = descriptionAndPremiumAmount.description,
        descriptionHtml = descriptionAndPremiumAmount.description,
        language = RU,
        expTime = expTime,
    )

    /**
     * Установить SRC-токен для конкретного заказа
     */
    private fun setSrcToken(payment: Payment) {
        val password = payment.paymentPageUrl?.substringAfter("password=")
        val orderId = payment.order?.id.toString()
        val url = "${apiConfigProperties.akbSbpUrl}/$orderId/$SET_SRC_TOKEN_SUFFIX$password"
        val body = SetSrcTokenRequest(token = mapOf(IPS_RU to true))

        val start = Instant.now()
        try {
            val response = postForObject<Map<String, Any>>(url, body)
            val duration = Duration.between(start, Instant.now())
            logger.info(
                "setSrcToken success (took ${formatDuration(duration)}): $response",
            )
        } catch (e: Exception) {
            val duration = Duration.between(start, Instant.now())
            logger.info(
                "Error setSrcToken (after ${formatDuration(duration)}): ${e.message}",
            )
            throw InnerException(getTraceId(), "Ошибка при установке SRC-токена: ${e.message}")
        }
    }

    /**
     * Подготовить push-транзакцию для конкретного заказа
     */
    private fun preparePushTran(payment: Payment): String? {
        val password = payment.paymentPageUrl?.substringAfter("password=")
        val orderId = payment.order?.id.toString()
        val url = "${apiConfigProperties.akbSbpUrl}/$orderId/$PUSH_TRAN_SUFFIX$password"
        val body =
            PreparePushTranRequest(
                specificByPm =
                    mapOf(
                        IPS_RU to mapOf(REDIRECT_URL to apiConfigProperties.backUrlS),
                    ),
            )
        val start = Instant.now()
        return try {
            val response = postForObject<PreparePushTranResponse?>(url, body)
            val duration = Duration.between(start, Instant.now())
            logger.info(
                "preparePushTran success (took ${formatDuration(duration)}): $response",
            )
            response
                ?.specificByPm
                ?.get(IPS_RU)
                ?.qrcPayload
        } catch (e: Exception) {
            val duration = Duration.between(start, Instant.now())
            logger.error(
                "Ошибка получения qrcPayload (after ${formatDuration(duration)}): ${e.message}",
            )
            throw InnerException(getTraceId(), "Ошибка при подготовке push-транзакции: ${e.message}")
        }
    }

    private fun postForCardPayment(request: AkbCardAndSbpPaymentRequest) =
        postForObject<AkbOrderResponse>(
            apiConfigProperties.akbUrl,
            HttpEntity(request, jsonHeaders()),
        )

    private fun postForSBPPaymentData(request: AkbCardAndSbpPaymentRequest) =
        postForObject<AkbOrderResponse>(
            apiConfigProperties.akbSbpUrl,
            HttpEntity(request, jsonHeaders()),
        )

    private inline fun <reified T> postForObject(
        url: String,
        request: Any? = null,
    ): T = restTemplate.postForObject<T>(url, request)

    private fun fillPaymentDataFromResponse(
        response: AkbOrderResponse,
        payment: Payment,
    ): Payment =
        payment.apply {
            state = PaymentStatusEnum.REG
            paymentPageUrl = response.order?.hppUrl.orEmpty()
            paymentBankId = response.order?.id.toString()
        }

    private fun validateRegisteredPayment(payment: Payment) {
        if (payment.paymentPageUrl.isNullOrBlank()) {
            logger.error(
                "$EMPTY_HPP_URL_RESPONSE [traceId=${getTraceId()}, orderId=${payment.paymentBankId}]",
            )
            throw InnerException(getTraceId(), EMPTY_HPP_URL_RESPONSE)
        }
    }

    override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse {
        TODO("Not yet implemented")
    }
}
