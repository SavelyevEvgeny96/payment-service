package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.request.ThreeDSTwo
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.gpb.GPBBankIntegrationGenerateDescriptionServiceImpl

@Mapper(componentModel = "spring")
abstract class GPBPaymentRequestMapper {
    @Autowired
    lateinit var tokenService: TokenService

    @Autowired
    lateinit var apiConfigProperties: ApiConfigProperties

    @Autowired
    lateinit var gPBBankIntegrationGenerateDescriptionServiceImpl: GPBBankIntegrationGenerateDescriptionServiceImpl

    companion object {
        private const val PAYMENT_PAGE = "payment_page"
        private const val URL_ONLY = "url_only"
        private const val IN_PROGRESS_STATE = "no"

        private const val SUBSCRIPTION_PURPOSE_PATTERN = "Подписка по договору страхования № %s"

        @JvmField
        val map = mapOf("card_on_file" to "MIT")

        @JvmField
        val cardPaymentState = State(PAYMENT_PAGE, IN_PROGRESS_STATE)

        @JvmField
        val cardPaymentStateRecurrent = State(URL_ONLY, IN_PROGRESS_STATE)

        @JvmField
        val cardPayment3ds2 = ThreeDSTwo(true)

        @JvmStatic
        @Named("getPaymentAmount")
        protected fun getPaymentAmount(payment: Payment): Int =
            payment.order
                .premiumAmount
                .toBigDecimal()
                .movePointRight(2)
                .intValueExact()

        @JvmStatic
        @Named("getSubscriptionPurpose")
        protected fun getSubscriptionPurpose(payment: Payment): String {
            val mainSubOrder = getMainSubOrder(payment)
            return SUBSCRIPTION_PURPOSE_PATTERN.format(mainSubOrder?.contractNumber)
        }

        @JvmStatic
        private fun getMainSubOrder(payment: Payment): SubOrder? = payment.order.subOrders.findLast(SubOrder::mainContractCheck)
    }

    @Mapping(target = "merchantId", expression = "java(getMerchantId(payment))")
    @Mapping(target = "merchantTrx", expression = "java(payment.getId().toString())")
    @Mapping(target = "token", expression = "java(tokenService.saveToken(payment))")
    @Mapping(target = "amount", expression = "java(getAmount(payment))")
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "description", expression = "java(getDescription(payment))")
    @Mapping(target = "state", expression = "java(cardPaymentStateRecurrent)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "params", expression = "java(map)")
    @Mapping(target = "depersonalization", source = "payment.depersonalization")
    @Mapping(target = "src", expression = "java(new Src(\"card_id\", payment.getKeyCard()))")
    @Mapping(target = "recurrent", constant = "true")
    @Mapping(target = "returnUrl", expression = "java(apiConfigProperties.getReturnUrl())")
    abstract fun toRecurrentRequest(payment: Payment): GPBPaymentRequest

    @Mapping(target = "merchantId", expression = "java(getMerchantId(payment))")
    @Mapping(target = "merchantTrx", expression = "java(payment.getOrder().getId().toString())")
    @Mapping(target = "token", expression = "java(tokenService.exchangeForToken(payment.getDepersonalization()))")
    @Mapping(target = "amount", expression = "java(getAmount(payment))")
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "description", expression = "java(getDescription(payment))")
    @Mapping(target = "state", expression = "java(cardPaymentState)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "addCardAllowed", expression = "java(getCardAllowed(payment))")
    @Mapping(target = "backUrlS", expression = "java(backUrlS(payment))")
    @Mapping(target = "backUrlF", expression = "java(backUrlF(payment))")
    @Mapping(target = "params", expression = "java(getParams(payment))")
    @Mapping(target = "depersonalization", source = "payment.depersonalization")
    @Mapping(target = "recurrent", constant = "false")
    @Mapping(target = "cardRegistration", source = "payment.order.regCard")
    abstract fun toCardRequest(payment: Payment): GPBPaymentRequest

    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "templateVersion", constant = "01")
    @Mapping(target = "qrTtl", constant = "60")
    @Mapping(target = "qrcType", constant = "02")
    @Mapping(target = "account", source = "properties.paymentAccount")
    @Mapping(target = "merchantId", source = "properties.merchantIdSbpGpb")
    @Mapping(target = "callbackMerchantNotifications", source = "properties.callbackUrlSbp")
    @Mapping(target = "paymentPurpose", expression = "java(getDescription(payment))")
    @Mapping(target = "amount", source = "payment", qualifiedByName = ["getPaymentAmount"])
    @Mapping(
        target = "subscriptionPurpose",
        conditionExpression = "java(payment.getSaveCard())",
        source = "payment",
        qualifiedByName = ["getSubscriptionPurpose"],
    )
    abstract fun toSbpRequest(
        payment: Payment,
        properties: ApiConfigProperties,
    ): GPBSBPPaymentRequest

    protected fun getCardAllowed(payment: Payment): Boolean? {
        if (!payment.order.regCard) {
            return payment.order.saveCard
        }
        return null
    }

    protected fun getAmount(payment: Payment): Int =
        payment.order
            .premiumAmount
            .toBigDecimal()
            .movePointRight(2)
            .intValueExact()

    protected fun getDescription(payment: Payment): String =
        gPBBankIntegrationGenerateDescriptionServiceImpl.makeDescription(payment.order).description

    protected fun getParams(payment: Payment): Map<String, String> =
        gPBBankIntegrationGenerateDescriptionServiceImpl.makeDescription(payment.order).params

    protected fun getMerchantId(payment: Payment): String = tokenService.takeMerchantId(payment.depersonalization)

    protected fun backUrlS(payment: Payment): String =
        payment.urlToReturn?.success()?.takeIf { it.isNotBlank() }
            ?: apiConfigProperties.backUrlS

    protected fun backUrlF(payment: Payment): String =
        payment.urlToReturn?.failed()?.takeIf { it.isNotBlank() }
            ?: apiConfigProperties.backUrlF
}
