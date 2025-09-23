package ru.sogaz.site.paymentService.service.payment.bank.integration

import org.springframework.http.HttpEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBQRImageRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties

class GPBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val restTemplate: RestTemplate,
) : BankIntegrationServiceImpl() {
    companion object {
        const val PAYMENT_PREFIX = "/payment/"
        const val START_PREFIX = "/start"
        const val PAYMENT_PAGE = "payment_page"
        const val TOKEN_PREFIX = "/token"
    }

    @Throws(BankIntegrationException::class, RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        exchangeForToken()
            .run { buildPaymentCardRequest(payment.order!!, this, payment.urlToReturn) }
            .run { postForCardPaymentLink(this) }
            .run { fillPaymentDataFromResponse(this, payment) }

    private fun exchangeForToken(): String {
        try {
            return formUrlForToken()
                .run { postForObject<GazpromTokenResponse>(this) }
                .run { this.token }
        } catch (ex: Exception) {
            throw BankIntegrationException(ActionType.GET_ACCESS_TOKEN_ERROR)
        }
    }

    private fun formUrlForToken(): String = "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$TOKEN_PREFIX"

    private fun buildPaymentCardRequest(
        order: Order,
        token: String,
        urlToReturn: UrlToReturn,
    ): GPBPaymentRequest {
        val descriptionAndPremiumAmount = generateDescription(order.premiumAmount, order.subOrders)
        return buildPaymentCardRequest(
            descAndPremiumAmountData = descriptionAndPremiumAmount,
            orderId = order.id.toString(),
            token = token,
            urlToReturn = urlToReturn,
        )
    }

    private fun buildPaymentCardRequest(
        descAndPremiumAmountData: DataDescriptionAndPremiumAmount,
        orderId: String,
        token: String,
        urlToReturn: UrlToReturn,
    ) = GPBPaymentRequest(
        portalId = apiConfigProperties.portalId,
        token = token,
        merchantId = apiConfigProperties.merchantId,
        orderId = orderId,
        backUrlS = urlToReturn.success() ?: apiConfigProperties.backUrlS,
        backUrlF = urlToReturn.failed() ?: apiConfigProperties.backUrlF,
        amount = descAndPremiumAmountData.premiumAmount,
        description = descAndPremiumAmountData.description,
        currency = RUB,
        state = State(redirect = PAYMENT_PAGE),
    )

    private fun formRegisterPaymentRequestUrl(token: String): String =
        "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$PAYMENT_PREFIX$token$START_PREFIX"

    private fun postForCardPaymentLink(request: GPBPaymentRequest) =
        postForObject<GazpromCardPaymentResponse>(
            formRegisterPaymentRequestUrl(request.token),
            HttpEntity(request, jsonHeaders()),
        )

    @Throws(RestClientException::class)
    override fun registerSBPPayment(payment: Payment): Payment =
        buildPaymentSBPRequest(payment.order!!)
            .run { postForSBPPaymentData(this) }
            .run { fillPaymentDataFromResponse(this, payment) }

    private fun buildPaymentSBPRequest(order: Order): GPBSBPPaymentRequest =
        generateDescription(order.premiumAmount, order.subOrders)
            .run(::buildPaymentSBPRequest)

    private fun buildPaymentSBPRequest(descAndPremiumAmountData: DataDescriptionAndPremiumAmount) =
        GPBSBPPaymentRequest(
            amount = descAndPremiumAmountData.premiumAmount,
            account = apiConfigProperties.paymentAccount,
            merchantId = apiConfigProperties.merchantIdSbpGpb,
            templateVersion = TEMPLATE_VERSION,
            qrTtl = QR_TTL,
            callbackMerchantNotifications = apiConfigProperties.callbackUrlSbp,
            qrcType = QR_TYPE,
            paymentPurpose = descAndPremiumAmountData.description,
            currency = RUB,
        )

    private fun postForSBPPaymentData(request: GPBSBPPaymentRequest) =
        postForObject<GazpromSBPPaymentResponse>(
            apiConfigProperties.gpbSbpUrl,
            HttpEntity(request, jsonHeaders()),
        )

    private inline fun <reified T> postForObject(
        url: String,
        request: Any? = null,
    ): T = restTemplate.postForObject<T>(url, request)

    private fun fillPaymentDataFromResponse(
        response: GazpromCardPaymentResponse,
        payment: Payment,
    ): Payment =
        payment.apply {
            state = PaymentStatusEnum.REG
            paymentPageUrl = response.options.paymentPageUrl
            paymentBankId = response.token
        }

    private fun fillPaymentDataFromResponse(
        response: GazpromSBPPaymentResponse,
        payment: Payment,
    ): Payment =
        payment.apply {
            state = PaymentStatusEnum.REG
            qrcId = response.data.qrcId
            paymentPageUrl = response.data.payload
            paymentBankId = response.transactionId
        }

    override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse =
        postForObject<GPBQRImageResponse>(
            apiConfigProperties.gpbSbpQRImageUrl,
            GPBQRImageRequest(payment.qrcId!!),
        )
}
