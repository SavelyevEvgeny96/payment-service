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
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import kotlin.time.measureTimedValue

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

    private val logger = loggerFor(javaClass)

    @Throws(BankIntegrationException::class, RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        exchangeForToken()
            .run { buildPaymentCardRequest(this, payment) }
            .run { postForCardPaymentLink(this) }
            .run { fillPaymentDataFromResponse(this, payment) }

    private fun exchangeForToken(): String {
        try {
            return postForObject<GazpromTokenResponse>(formUrlForToken()).token
        } catch (ex: Exception) {
            throw BankIntegrationException(ActionType.GET_ACCESS_TOKEN_ERROR)
        }
    }

    private fun formUrlForToken(): String = "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$TOKEN_PREFIX"

    private fun buildPaymentCardRequest(
        token: String,
        payment: Payment,
    ): GPBPaymentRequest {
        val descriptionAndPremiumAmount = generateDescription(payment)
        return buildPaymentCardRequest(
            descAndPremiumAmountData = descriptionAndPremiumAmount,
            orderId = payment.order?.id.toString(),
            token = token,
            urlToReturn = payment.urlToReturn,
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
        amount = descAndPremiumAmountData.premiumAmount.toString(),
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
        payment
            .run(::buildPaymentSBPRequest)
            .run(::postForSBPPaymentData)
            .run { fillPaymentDataFromResponse(this, payment) }

    private fun buildPaymentSBPRequest(payment: Payment): GPBSBPPaymentRequest =
        payment
            .run(::generateDescription)
            .run(::buildPaymentSBPRequest)

    private fun buildPaymentSBPRequest(descAndPremiumAmountData: DataDescriptionAndPremiumAmount) =
        GPBSBPPaymentRequest(
            amount = descAndPremiumAmountData.premiumAmount.toString(),
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
    ): T = postForObjectWithLogging(url, request)

    private inline fun <reified T> postForObjectWithLogging(
        url: String,
        request: Any? = null,
    ): T {
        logger.info("Prepare for POST GPB request: $url with body: $request")
        return withMeasureTime { restTemplate.postForObject<T>(url, request) }
            .also { logger.info("Successfully processing request for GPB with response: $it") }
    }

    private inline fun <reified T> withMeasureTime(block: () -> T): T {
        val (value: T, timeTaken) = measureTimedValue(block)
        logger.info("${timeTaken.inWholeSeconds} whole seconds taken for GPB request")
        return value
    }

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
