package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.paymentService.dto.data.AmountData
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.State
import ru.sogaz.site.paymentService.dto.request.ThreeDSTwo
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.service.TokenService
import ru.sogaz.site.paymentService.service.bank.integration.gpb.GPBBankIntegrationHelperServiceImpl

@Mapper(componentModel = "spring")
abstract class GPBPaymentRequestMapper {

    @Autowired
    lateinit var tokenService: TokenService
    @Autowired
    protected lateinit var gpbBankIntegrationHelper: GPBBankIntegrationHelperServiceImpl

    companion object {
        private const val URL_ONLY = "url_only"
        private const val IN_PROGRESS_STATE = "no"
        val map = mapOf("card_on_file" to "MIT")
        val cardPaymentStateRecurrent = State(redirect = URL_ONLY, inProgress = IN_PROGRESS_STATE)
        val cardPayment3ds2 = ThreeDSTwo(true)
    }

    @Mapping(target = "merchantId", expression = "java(getMerchantId(payment))")
    @Mapping(target = "token", expression = "java(tokenService.saveToken(payment))")
    @Mapping(target = "amount", expression = "java(payment.getAmountData().getAmountInPennies())")
    @Mapping(target = "currency", expression = "java(payment.getAmountData().currency)")
    @Mapping(target = "description", expression = "java(getDescription(payment))")
    @Mapping(target = "state", constant = "java(cardPaymentStateRecurrent)")
    @Mapping(target = "threeDSTwo", constant = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "params", expression = "java(map)")
    @Mapping(target = "depersonalization", source = "payment.depersonalization")
    @Mapping(target = "src", expression = "java(new Src(\"card_id\", payment.getKeyCard()))")
    @Mapping(target = "recurrent", constant = "true")
    @Mapping(target = "returnUrl", expression = "payment.returnUrl")
    abstract fun toRecurrentRequest(payment: Payment): GPBPaymentRequest

    // --------------------------------------------
    // методы для вычисления полей
    // --------------------------------------------
    protected fun Payment.getAmountData() = AmountData(
            amount = this.order!!.premiumAmount.toBigDecimal(),
            currency = CurrencyEnum.RUB,
        )
    protected fun getDescription(payment: Payment): String =
        gpbBankIntegrationHelper.makeDescription(payment.order!!).description
    protected fun getParams(payment: Payment): Map<String, String> =
        gpbBankIntegrationHelper.makeDescription(payment.order!!).params
    protected fun getToken(payment: Payment) = tokenService.saveToken(payment)
    protected fun getMerchantId(payment: Payment): String = tokenService.takeMerchantId(payment.depersonalization)
}