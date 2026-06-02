package ru.sogaz.site.paymentService.mapper.v2.bank.abr.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.paymentService.dto.request.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.request.PreparePushTranRequest
import ru.sogaz.site.paymentService.dto.request.SetSrcTokenRequest
import ru.sogaz.site.paymentService.enums.TypeRidEnum
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Mapper
abstract class AbrRequestMapper {
    @Autowired
    lateinit var apiConfigProperties: ApiConfigProperties

    companion object {
        private const val REDIRECT_URL_PARAM_NAME = "afterPayRedirectUrl"
        private const val IPS_RU_PARAM_NAME = "ipsRu"
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        @JvmStatic
        @Named("mapRequestAmount")
        fun mapRequestAmount(amount: BigDecimal): Int = amount.setScale(0, java.math.RoundingMode.HALF_UP).toInt()

        @JvmStatic
        @Named("mapExpTime")
        fun mapExpTime(typeRid: TypeRidEnum): String? =
            LocalDateTime.now().plusMinutes(15).format(formatter).takeIf { typeRid == TypeRidEnum.QRC_PAY }
    }

    fun toCardRequest(cardPayOperationRequest: CardPayOperationRequest): AbrCardAndSbpPaymentRequest =
        toAbrRequest(cardPayOperationRequest, TypeRidEnum.WITH_3DS)

    fun toSbpRequest(sbpPayOperationRequest: SbpPayOperationRequest): AbrCardAndSbpPaymentRequest =
        toAbrRequest(sbpPayOperationRequest, TypeRidEnum.QRC_PAY)

    fun toSetSrcTokenRequest(): SetSrcTokenRequest = SetSrcTokenRequest(token = mapOf(IPS_RU_PARAM_NAME to true))

    fun toPreparePushTranRequest(redirectParams: RedirectParams): PreparePushTranRequest =
        PreparePushTranRequest(
            specificByPm = mapOf(
                IPS_RU_PARAM_NAME to mapOf(REDIRECT_URL_PARAM_NAME to redirectParams.successUrl()),
            ),
        )

    @Mapping(target = "order.typeRid", source = "typeRid")
    @Mapping(target = "order.ridByMerchant", source = "request.orderId")
    @Mapping(target = "order.amount", source = "request.amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "order.currency", constant = "RUB")
    @Mapping(target = "order.hppRedirectUrl", expression = "java(successUrl(request.getParams()))")
    @Mapping(target = "order.adviceIfaceAddress", expression = "java(successUrl(request.getParams()))")
    @Mapping(target = "order.description", source = "request.description")
    @Mapping(target = "order.descriptionHtml", source = "request.description")
    @Mapping(target = "order.expTime", source = "typeRid", qualifiedByName = ["mapExpTime"])
    protected abstract fun toAbrRequest(
        request: CardPayOperationRequest,
        typeRid: TypeRidEnum,
    ): AbrCardAndSbpPaymentRequest

    @Mapping(target = "order.typeRid", source = "typeRid")
    @Mapping(target = "order.ridByMerchant", source = "request.orderId")
    @Mapping(target = "order.amount", source = "request.amount", qualifiedByName = ["mapRequestAmount"])
    @Mapping(target = "order.currency", constant = "RUB")
    @Mapping(target = "order.hppRedirectUrl", expression = "java(successUrl(request.getParams()))")
    @Mapping(target = "order.adviceIfaceAddress", expression = "java(successUrl(request.getParams()))")
    @Mapping(target = "order.description", source = "request.description")
    @Mapping(target = "order.descriptionHtml", source = "request.description")
    @Mapping(target = "order.expTime", source = "typeRid", qualifiedByName = ["mapExpTime"])
    protected abstract fun toAbrRequest(
        request: SbpPayOperationRequest,
        typeRid: TypeRidEnum,
    ): AbrCardAndSbpPaymentRequest

    protected fun successUrl(redirectParams: RedirectParams): String =
        redirectParams.urlToReturnS ?: redirectParams.urlToReturn ?: apiConfigProperties.backUrlS
}
