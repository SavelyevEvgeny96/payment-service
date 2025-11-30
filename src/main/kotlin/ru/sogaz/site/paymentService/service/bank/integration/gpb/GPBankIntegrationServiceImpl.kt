package ru.sogaz.site.paymentService.service.bank.integration.gpb

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpPaymentClient
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.DescriptionInfo
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBQRImageRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBStatusSBPRequest
import ru.sogaz.site.paymentService.dto.request.Src
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.request.ThreeDSTwo
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.HeaderStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.payment.GPBPaymentRequestMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationServiceImpl

@Service
class GPBankIntegrationServiceImpl(
    private val apiConfigProperties: ApiConfigProperties,
    private val gpbSbpPaymentClient: GpbSbpPaymentClient,
    private val gpbCardPaymentClient: GpbCardPaymentClient,
    private val gpbBankIntegrationHelperServiceImpl: GPBBankIntegrationHelperServiceImpl,
    private val gpBPaymentRequestMapper: GPBPaymentRequestMapper,
    private val tokenService: TokenService, // ⬅ новый сервис токенов
) : BankIntegrationServiceImpl() {
    companion object {
        private const val TEMPLATE_VERSION = "01"
        private const val QR_TTL = "60"
        private const val QR_TYPE = "02"

        private const val PAYMENT_PAGE = "payment_page"
        private const val URL_ONLY = "url_only"
        private const val IN_PROGRESS_STATE = "no"
        private val map = mapOf("card_on_file" to "MIT")
        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции:"
        private val cardPaymentState = State(redirect = PAYMENT_PAGE, inProgress = IN_PROGRESS_STATE)
        private val cardPaymentStateRecurrent = State(redirect = URL_ONLY, inProgress = IN_PROGRESS_STATE)
        private val cardPayment3ds2 = ThreeDSTwo(true)
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

    @Throws(BankIntegrationException::class, RestClientException::class)
    override fun registerCardPaymentRecurrent(payment: Payment): Payment =
        payment
            .let { gpBPaymentRequestMapper.toRecurrentRequest(it) }
            .run(::postForCardPaymentLinkRecurrent)
            .run { payment.fillBankRegistration(this) }

    private fun buildPaymentCardRequest(
        token: String,
        order: Order,
        amountData: AmountData,
        depersonalization: Boolean,
        descriptionInfo: DescriptionInfo,
        urlToReturn: UrlToReturn,
    ) = GPBPaymentRequest(
        merchantId = tokenService.takeMerchantId(depersonalization),
        merchantTrx = order.id.toString(),
        token = token,
        backUrlS = urlToReturn.success() ?: apiConfigProperties.backUrlS,
        backUrlF = urlToReturn.failed() ?: apiConfigProperties.backUrlF,
        amount = amountData.getAmountInPennies(),
        description = descriptionInfo.description,
        currency = amountData.currency,
        state = cardPaymentState,
        threeDSTwo = cardPayment3ds2,
        openApiMirPaySupported = true,
        addCardAllowed = order.saveCard,
        params = descriptionInfo.params,
        depersonalization = depersonalization,
    )

    private fun buildPaymentCardRequestRecurrent(
        token: String,
        payment: Payment,
        amountData: AmountData,
        depersonalization: Boolean,
        descriptionInfo: DescriptionInfo,
    ) = GPBPaymentRequest(
        merchantId = tokenService.takeMerchantId(depersonalization),
        merchantTrx = payment.id.toString(),
        token = token,
        amount = amountData.getAmountInPennies(),
        description = descriptionInfo.description,
        currency = amountData.currency,
        state = cardPaymentStateRecurrent,
        threeDSTwo = cardPayment3ds2,
        openApiMirPaySupported = true,
        params = map,
        depersonalization = depersonalization,
        src = Src(type = "card_id", cardId = payment.keyCard),
        recurrent = true,
        returnUrl = apiConfigProperties.returnUrl,
    )

    private fun postForCardPaymentLink(request: GPBPaymentRequest): GazpromCardPaymentResponse =
        gpbCardPaymentClient.startPayment(
            tokenService.takePortalId(request.depersonalization),
            request.token,
            request,
        )

    private fun postForCardPaymentLinkRecurrent(request: GPBPaymentRequest): RegisterCardResponseDto =
        gpbCardPaymentClient.startPaymentRecurrent(
            tokenService.takePortalId(request.depersonalization),
            request.token,
            request,
        )

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
    ): Payment =
        payment
            .run(::buildPaymentSBPRequest)
            .run { postForSBPPaymentData(this, headersParams) }
            .run { payment.fillFromResponse(this) }

    private fun buildPaymentSBPRequest(payment: Payment): GPBSBPPaymentRequest =
        buildPaymentSBPRequest(payment.getAmountData(), payment.v4Description())

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
        headersParams
            .run(::sbpHeaders)
            .run { gpbSbpPaymentClient.startPayment(this, request) }

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

    private fun Payment.fillBankRegistration(response: RegisterCardResponseDto) =
        apply {
            state =
                if (response.result?.status == StatusEnum.SUCCESS.value) {
                    PaymentStatusEnum.REG
                } else {
                    PaymentStatusEnum.FAIL
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

    private fun requestCardPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        gpbCardPaymentClient
            .getPaymentStatus(
                tokenService.takePortalId(paymentBankInfo.depersonalization),
                paymentBankInfo.paymentBankId,
            ).toBankPaymentDetails()

    private fun requestSBPPaymentStatus(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        gpbSbpPaymentClient
            .getPaymentStatus(GPBStatusSBPRequest(paymentBankInfo.paymentBankId))
            .toBankPaymentDetails()

    private fun GpbCardPaymentStatusResponse.toBankPaymentDetails(): BankPaymentDetails =
        gpbBankIntegrationHelperServiceImpl.convertToBankPaymentDetails(this)

    private fun GpbSbpPaymentStatusResponse.toBankPaymentDetails(): BankPaymentDetails =
        gpbBankIntegrationHelperServiceImpl.convertToBankPaymentDetails(this)
}
