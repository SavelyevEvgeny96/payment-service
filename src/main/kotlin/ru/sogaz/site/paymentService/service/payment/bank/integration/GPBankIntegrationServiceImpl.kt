package ru.sogaz.site.paymentService.service.payment.bank.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBQRImageRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.request.ThreeDSTwo
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import kotlin.time.measureTimedValue

class GPBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val restTemplate: RestTemplate,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
) : BankIntegrationServiceImpl() {
    companion object {
        private const val PAYMENT_PATH = "/payment/"
        private const val START_PATH = "/start"
        private const val TOKEN_PATH = "/token"

        private const val PAYMENT_PAGE = "payment_page"
        private const val IN_PROGRESS_STATE = "no"

        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции:"

        private val cardPaymentState = State(redirect = PAYMENT_PAGE, inProgress = IN_PROGRESS_STATE)
        private val cardPayment3ds2 = ThreeDSTwo(true)
    }

    private val logger = loggerFor(javaClass)

    @Throws(BankIntegrationException::class, RestClientException::class)
    override fun registerCardPayment(payment: Payment): Payment =
        payment
            .run(::buildPaymentCardRequest)
            .run(::postForCardPaymentLink)
            .run { payment.fillFromResponse(this) }

    private fun buildPaymentCardRequest(payment: Payment): GPBPaymentRequest =
        buildPaymentCardRequest(
            token = exchangeForToken(),
            order = payment.order!!,
            amountData = payment.getAmountData(),
            description = payment.getDescription(),
            urlToReturn = payment.urlToReturn,
        )

    private fun exchangeForToken(): String {
        try {
            return postForObject<GazpromTokenResponse>(formUrlForToken()).token
        } catch (ex: Exception) {
            throw BankIntegrationException(ActionType.GET_ACCESS_TOKEN_ERROR)
        }
    }

    private fun formUrlForToken(): String = "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$TOKEN_PATH"

    private fun buildPaymentCardRequest(
        token: String,
        order: Order,
        amountData: AmountData,
        description: String,
        urlToReturn: UrlToReturn,
    ) = GPBPaymentRequest(
        merchantId = apiConfigProperties.merchantId,
        orderId = order.id.toString(),
        token = token,
        backUrlS = urlToReturn.success() ?: apiConfigProperties.backUrlS,
        backUrlF = urlToReturn.failed() ?: apiConfigProperties.backUrlF,
        amount = amountData.getAmountInPennies(),
        description = description,
        currency = amountData.currency,
        state = cardPaymentState,
        threeDSTwo = cardPayment3ds2,
        openApiMirPaySupported = true,
        addCardAllowed = order.saveCard,
    )

    private fun formRegisterPaymentRequestUrl(token: String): String =
        "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$PAYMENT_PATH$token$START_PATH"

    private fun postForCardPaymentLink(request: GPBPaymentRequest): GazpromCardPaymentResponse =
        postForObject(
            formRegisterPaymentRequestUrl(request.token),
            HttpEntity(request, jsonHeaders),
        )

    @Throws(RestClientException::class)
    override fun registerSBPPayment(
        payment: Payment,
        headersParams: GpbSbpHeadersParams?,
    ): Payment =
        payment
            .run(::buildPaymentSBPRequest)
            .run { postForSBPPaymentData(this, headersParams) }
            .run { payment.fillFromResponse(this) }

    private fun buildPaymentSBPRequest(payment: Payment): GPBSBPPaymentRequest =
        buildPaymentSBPRequest(
            payment.getAmountData(),
            payment.getDescription(),
        )

    private fun buildPaymentSBPRequest(
        amountData: AmountData,
        description: String,
    ) = GPBSBPPaymentRequest(
        amount = amountData.getAmount(),
        account = apiConfigProperties.paymentAccount,
        merchantId = apiConfigProperties.merchantIdSbpGpb,
        templateVersion = TEMPLATE_VERSION,
        qrTtl = QR_TTL,
        callbackMerchantNotifications = apiConfigProperties.callbackUrlSbp,
        qrcType = QR_TYPE,
        paymentPurpose = description,
        currency = amountData.currency,
    )

    private fun postForSBPPaymentData(
        request: GPBSBPPaymentRequest,
        headersParams: GpbSbpHeadersParams?,
    ): GazpromSBPPaymentResponse =
        postForObject(
            apiConfigProperties.gpbSbpUrl,
            HttpEntity(request, sbpHeaders(headersParams)),
        )

    private fun sbpHeaders(headersParams: GpbSbpHeadersParams?) =
        HttpHeaders()
            .apply {
                contentType = MediaType.APPLICATION_JSON
                set("paymentDelay", headersParams?.paymentDelay)
                set("processPayments", headersParams?.processPayments)
                set("paymentStatus", headersParams?.paymentStatus)
            }

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

    private fun Payment.fillFromResponse(response: GazpromCardPaymentResponse) =
        this.apply {
            state = PaymentStatusEnum.REG
            paymentPageUrl = response.options.paymentPageUrl
            paymentBankId = response.token
        }

    private fun Payment.fillFromResponse(response: GazpromSBPPaymentResponse) =
        this.apply {
            state = PaymentStatusEnum.REG
            qrcId = response.data.qrcId
            paymentPageUrl = response.data.payload
            paymentBankId = response.transactionId
        }

    override fun getQRCodeImageData(payment: Payment): GPBQRImageResponse =
        postForObject(
            apiConfigProperties.gpbSbpQRImageUrl,
            HttpEntity(GPBQRImageRequest(payment.qrcId!!), jsonHeaders),
        )

    override fun requestPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        try {
            when (paymentBankInfo.type) {
                PaymentTypeEnum.CARD -> requestCardPaymentStatus(paymentBankInfo)
                PaymentTypeEnum.SBP -> requestSBPPaymentStatus(paymentBankInfo)
            }
        } catch (ex: RestClientException) {
            logger.info("$LOG_GPB_API_ERROR ${paymentBankInfo.paymentBankId}", ex)
            throw InnerException(getTraceId(), "$LOG_GPB_API_ERROR ${paymentBankInfo.paymentBankId}")
        }

    private fun requestCardPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails {
        val url = "${apiConfigProperties.gpbUrl}${apiConfigProperties.portalId}$PAYMENT_PATH${paymentBankInfo.paymentBankId}"
        return restTemplate
            .postForObject<GpbCardPaymentStatusResponse>(url)
            .toBankPaymentDetails()
    }

    private fun requestSBPPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails {
        val url = apiConfigProperties.gpbSbpUrlStatus
        val requestBody = ObjectMapper().writeValueAsString(mapOf("qrcIds" to listOf(paymentBankInfo.qrcId)))
        return restTemplate
            .postForObject<GpbSbpPaymentStatusResponse>(url, HttpEntity(requestBody, jsonHeaders))
            .toBankPaymentDetails()
    }

    private fun GpbCardPaymentStatusResponse.toBankPaymentDetails(): BankPaymentDetails =
        bankPaymentDetailsMapper.convert(this, getStatus())

    private fun GpbCardPaymentStatusResponse.getStatus(): StatusEnum = result?.status ?: StatusEnum.WAIT

    private fun GpbSbpPaymentStatusResponse.toBankPaymentDetails(): BankPaymentDetails = bankPaymentDetailsMapper.convert(this, getStatus())

    private fun GpbSbpPaymentStatusResponse.getStatus(): StatusEnum = result.firstOrNull()?.status ?: StatusEnum.WAIT
}
