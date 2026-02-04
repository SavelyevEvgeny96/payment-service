package ru.sogaz.site.paymentService.mapper.v2.bank.gpb

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequestDepr
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.State
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.ThreeDSTwo
import ru.sogaz.site.paymentService.model.v2.web.request.PayParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import java.math.BigDecimal

@Mapper
abstract class GpbRequestMapper {
    companion object {
        private const val PAYMENT_PAGE = "payment_page"
        private const val IN_PROGRESS_STATE = "no"

        @JvmField
        val cardPaymentState = State(PAYMENT_PAGE, IN_PROGRESS_STATE)

        @JvmField
        val cardPayment3ds2 = ThreeDSTwo(true)

        @JvmStatic
        @Named("mapRequestAmount")
        fun mapRequestAmount(amount: BigDecimal): Int =
            amount
                .movePointRight(2)
                .intValueExact()
    }

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "params", source = "cardPayOperationRequest.payItems")
    @Mapping(target = "merchantTrx", source = "cardPayOperationRequest.orderId")
    @Mapping(target = "addCardAllowed", source = "payParams.saveCard")
    @Mapping(target = "state", expression = "java(cardPaymentState)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "currency", constant = "RUB")
    abstract fun toCardRequest(
        merchantId: String,
        cardPayOperationRequest: CardPayOperationRequest,
        payParams: PayParams,
    ): GpbPayRequestDepr
}
