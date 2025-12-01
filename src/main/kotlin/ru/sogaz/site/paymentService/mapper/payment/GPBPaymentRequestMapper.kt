package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.request.ThreeDSTwo
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.CurrencyEnum
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
    lateinit var gpbBankIntegrationHelper: GPBBankIntegrationGenerateDescriptionServiceImpl

    companion object {
        private const val PAYMENT_PAGE = "payment_page"
        private const val URL_ONLY = "url_only"
        private const val IN_PROGRESS_STATE = "no"

        @JvmField
        val map = mapOf("card_on_file" to "MIT")

        @JvmField
        val cardPaymentState = State(PAYMENT_PAGE, IN_PROGRESS_STATE)

        @JvmField
        val cardPaymentStateRecurrent = State(URL_ONLY, IN_PROGRESS_STATE)

        @JvmField
        val cardPayment3ds2 = ThreeDSTwo(true)
    }

    @Mapping(target = "merchantId", expression = "java(getMerchantId(payment))")
    @Mapping(target = "merchantTrx", expression = "java(payment.getId().toString())")
    @Mapping(target = "token", expression = "java(tokenService.saveToken(payment))")
    @Mapping(target = "amount", expression = "java(getAmount(payment))")
    @Mapping(target = "currency", expression = "java(getCurrency(payment))")
    @Mapping(target = "description", expression = "java(getDescription(payment))")
    @Mapping(target = "state", expression = "java(cardPaymentStateRecurrent)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "params", expression = "java(getParams(payment))")
    @Mapping(target = "depersonalization", source = "payment.depersonalization")
    @Mapping(target = "src", expression = "java(new Src(\"card_id\", payment.getKeyCard()))")
    @Mapping(target = "recurrent", constant = "true")
    @Mapping(target = "returnUrl", expression = "java(apiConfigProperties.getReturnUrl())")
    abstract fun toRecurrentRequest(payment: Payment): GPBPaymentRequest

    @Mapping(target = "merchantId", expression = "java(getMerchantId(payment))")
    @Mapping(target = "merchantTrx", expression = "java(payment.getOrder().getId().toString())")
    @Mapping(target = "token", expression = "java(tokenService.exchangeForToken(payment.getDepersonalization()))")
    @Mapping(target = "amount", expression = "java(getAmount(payment))")
    @Mapping(target = "currency", expression = "java(getCurrency(payment))")
    @Mapping(target = "description", expression = "java(getDescription(payment))")
    @Mapping(target = "state", expression = "java(cardPaymentState)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "addCardAllowed", expression = "java(payment.getOrder().getSaveCard())")
    @Mapping(target = "backUrlS", expression = "java(backUrlS(payment))")
    @Mapping(target = "backUrlF", expression = "java(backUrlF(payment))")
    @Mapping(target = "params", expression = "java(getParams(payment))")
    @Mapping(target = "depersonalization", source = "payment.depersonalization")
    @Mapping(target = "recurrent", constant = "false")
    abstract fun toCardRequest(payment: Payment): GPBPaymentRequest

    protected fun getAmount(payment: Payment): Int =
        payment.order!!
            .premiumAmount
            .toBigDecimal()
            .movePointRight(2)
            .intValueExact()

    protected fun getCurrency(payment: Payment) = CurrencyEnum.RUB

    protected fun getDescription(payment: Payment): String = gpbBankIntegrationHelper.makeDescription(payment.order!!).description

    protected fun getParams(payment: Payment): Map<String, String> = gpbBankIntegrationHelper.makeDescription(payment.order!!).params

    protected fun getMerchantId(payment: Payment): String = tokenService.takeMerchantId(payment.depersonalization)

    protected fun backUrlS(payment: Payment): String =
        payment.urlToReturn.success()?.takeIf { it.isNotBlank() }
            ?: apiConfigProperties.backUrlS

    protected fun backUrlF(payment: Payment): String =
        payment.urlToReturn.failed()?.takeIf { it.isNotBlank() }
            ?: apiConfigProperties.backUrlF
}
