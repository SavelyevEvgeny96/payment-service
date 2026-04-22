package ru.sogaz.site.paymentService.mapper.v2.bank.abr.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.enums.TypeRidEnum
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrOrderDto
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrPreparePushTranRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrSetSrcTokenRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import java.math.BigDecimal

@Mapper
abstract class AbrRequestMapper {
    companion object {
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private const val REDIRECT_URL_PARAM_NAME = "afterPayRedirectUrl"

        @JvmStatic
        @Named("mapRequestAmount")
        fun mapRequestAmount(amount: BigDecimal): Int =
            amount
                .movePointRight(2)
                .intValueExact()
    }

    @Mapping(target = "typeRid", expression = "java( TypeRidEnum.WITH_3DS )")
    @Mapping(target = "ridByMerchant", source = "request.orderId")
    @Mapping(target = "amount", source = "request.amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "hppRedirectUrl", source = "redirectUrl")
    @Mapping(target = "adviceIfaceAddress", source = "redirectUrl")
    @Mapping(target = "descriptionHtml", source = "request.description")
    protected abstract fun toCardOrder(
        request: CardPayOperationRequest,
        redirectUrl: String,
    ): AbrOrderDto

    fun toCardPaymentRequest(
        request: CardPayOperationRequest,
        redirectUrl: String,
    ): AbrCardAndSbpPaymentRequest = AbrCardAndSbpPaymentRequest(toCardOrder(request, redirectUrl))

    @Mapping(target = "typeRid", expression = "java( TypeRidEnum.QRC_PAY )")
    @Mapping(target = "ridByMerchant", source = "request.orderId")
    @Mapping(target = "amount", source = "request.amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "hppRedirectUrl", source = "redirectUrl")
    @Mapping(target = "adviceIfaceAddress", source = "redirectUrl")
    @Mapping(target = "descriptionHtml", source = "request.description")
    @Mapping(target = "expTime", source = "expirationDate")
    protected abstract fun toSbpOrder(
        request: SbpPayOperationRequest,
        redirectUrl: String,
        expirationDate: String,
    ): AbrOrderDto

    fun toSbpPaymentRequest(
        request: SbpPayOperationRequest,
        redirectUrl: String,
        expirationDate: String,
    ): AbrCardAndSbpPaymentRequest = AbrCardAndSbpPaymentRequest(toSbpOrder(request, redirectUrl, expirationDate))

    fun toSetSrcTokenRequest(): AbrSetSrcTokenRequest = AbrSetSrcTokenRequest(token = mapOf(IPS_RU_PARAM_NAME to true))

    fun toPreparePushTranRequest(redirectUrl: String): AbrPreparePushTranRequest =
        AbrPreparePushTranRequest(specificByPm = mapOf(IPS_RU_PARAM_NAME to mapOf(REDIRECT_URL_PARAM_NAME to redirectUrl)))
}
