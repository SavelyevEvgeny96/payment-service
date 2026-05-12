package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAccountData
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbQrImageRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbRefundParams
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSpbStatusRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.Src
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.State
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.ThreeDSTwo
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import java.math.BigDecimal

@Mapper
abstract class GpbRequestMapper {
    companion object {
        private const val PAYMENT_PAGE = "payment_page"
        private const val RECURRENT_REDIRECT = "no"
        private const val IN_PROGRESS_STATE = "no"
        private const val SCR_TYPE_FOR_CARD = "card_id"

        @JvmField
        val cardPaymentState = State(PAYMENT_PAGE, IN_PROGRESS_STATE)

        @JvmField
        val cardPaymentStateRecurrent = State(RECURRENT_REDIRECT, IN_PROGRESS_STATE)

        @JvmField
        val cardPayment3ds2 = ThreeDSTwo(true)

        @JvmStatic
        @Named("mapRequestAmount")
        fun mapRequestAmount(amount: BigDecimal): Int =
            amount
                .movePointRight(2)
                .intValueExact()

        @JvmStatic
        fun requireQrId(bankPaymentPageData: BankPaymentPageData): String =
            requireNotNull(bankPaymentPageData.qrId) { "QR id is missing for SBP QR image request" }

        @JvmStatic
        @Named("mapSrc")
        fun mapSrc(keyCard: String): Src = Src(SCR_TYPE_FOR_CARD, keyCard)
    }

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "params", source = "request.payItems")
    @Mapping(target = "merchantTrx", source = "request.orderId")
    @Mapping(target = "state", expression = "java(cardPaymentStateRecurrent)")
    @Mapping(target = "src", source = "request.keyCard", qualifiedByName = ["mapSrc"])
    @Mapping(target = "recurrent", constant = "true")
    @Mapping(target = "currency", constant = "RUB")
    abstract fun toRecurrentRequest(
        merchantId: String,
        request: CardRecurrentOperationRequest,
    ): GpbPayRequest

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "params", source = "request.payItems")
    @Mapping(target = "merchantTrx", source = "request.orderId")
    @Mapping(target = "addCardAllowed", source = "request.saveCard")
    @Mapping(target = "state", expression = "java(cardPaymentState)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "currency", constant = "RUB")
    abstract fun toCardRequest(
        merchantId: String,
        request: CardPayOperationRequest,
        redirectParams: RedirectParams,
    ): GpbPayRequest

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "params", source = "request.payItems")
    @Mapping(target = "merchantTrx", source = "request.orderId")
    @Mapping(target = "state", expression = "java(cardPaymentState)")
    @Mapping(target = "threeDSTwo", expression = "java(cardPayment3ds2)")
    @Mapping(target = "openApiMirPaySupported", constant = "true")
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "cardRegistration", constant = "true")
    abstract fun toRegRequest(
        merchantId: String,
        request: PayRegOperationRequest,
        redirectParams: RedirectParams,
    ): GpbPayRequest

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "account", source = "gpbSbpAccountData.paymentAccount")
    @Mapping(target = "merchantId", source = "gpbSbpAccountData.merchantIdSbpGpb")
    @Mapping(target = "callbackMerchantNotifications", source = "gpbSbpAccountData.callbackUrlSbp")
    @Mapping(target = "paymentPurpose", source = "sbpPayOperationRequest.description")
    @Mapping(target = "qrTtl", source = "gpbSbpAccountData.qrcTtl")
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "templateVersion", constant = "01")
    @Mapping(target = "qrcType", constant = "02")
    abstract fun toSbpRequest(
        sbpPayOperationRequest: SbpPayOperationRequest,
        gpbSbpAccountData: GpbSbpAccountData,
        redirectUrl: String,
    ): GpbSbpPayRequest

    fun toSbpStatusRequest(sbpPayOperation: SbpPayOperation): GpbSpbStatusRequest = GpbSpbStatusRequest(sbpPayOperation.paymentBankId)

    @Mapping(target = "qrcId", expression = "java(requireQrId(bankPaymentPageData))")
    @Mapping(target = "width", constant = "300")
    @Mapping(target = "height", constant = "300")
    abstract fun toQrImageRequest(bankPaymentPageData: BankPaymentPageData): GpbQrImageRequest

    @Mapping(target = "amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "comment", source = "description")
    abstract fun toRefundParams(refundOperationRequest: RefundOperationRequest): GpbRefundParams
}
