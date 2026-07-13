package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid.GpbGidPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidPayResponse
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GidPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties
import java.math.BigDecimal

/**
 * Маппер запросов и ответов Газпромбанка для оплаты банковской картой в ГИД.
 */
@Mapper(imports = [BankEnum::class])
abstract class GpbGidMapper {
    @Mapping(target = "gid", source = "request.params.gid")
    @Mapping(target = "ecoCardId", source = "request.params.cardId")
    @Mapping(target = "portalId", expression = "java(mapPortalId(request, properties))")
    @Mapping(target = "merchantId", expression = "java(mapMerchantId(request, properties))")
    @Mapping(target = "accountId", expression = "java(mapAccountId(request, properties))")
    @Mapping(target = "merchantTrx", source = "request.orderId")
    @Mapping(target = "currency", constant = RUB_CURRENCY)
    @Mapping(target = "amount", source = "request.amount", qualifiedByName = ["mapAmount"])
    @Mapping(target = "params", source = "request.payItems", qualifiedByName = ["emptyToNull"])
    @Mapping(target = "backUrlSuccess", expression = "java(mapBackUrlSuccess(request, properties))")
    @Mapping(target = "backUrlFail", expression = "java(mapBackUrlFail(request, properties))")
    @Mapping(target = "lang", constant = RU_LANGUAGE)
    @Mapping(target = "addCardAllowed", source = "request.saveCard", qualifiedByName = ["trueOrNull"])
    abstract fun toGpbGidPayRequest(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): GpbGidPayRequest

    @Mapping(target = "bank", expression = "java(BankEnum.GPB)")
    @Mapping(target = "paymentBankId", source = "pgaTrxId")
    @Mapping(target = "paymentPageUrl", source = "paymentPageUrl")
    abstract fun toBankPaymentPageData(response: GpbGidPayResponse): BankPaymentPageData

    fun mapPortalId(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): String = if (request.depersonalization) properties.depersonalizedPortalId else properties.portalId

    fun mapMerchantId(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): String = if (request.depersonalization) properties.depersonalizedMerchantId else properties.merchantId

    fun mapAccountId(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): String = if (request.depersonalization) properties.depersonalizedAccountId else properties.accountId

    fun mapBackUrlSuccess(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): String = request.params.urlToReturnS ?: properties.backUrlSuccess

    fun mapBackUrlFail(
        request: GidPayOperationRequest,
        properties: GpbGidProperties,
    ): String = request.params.urlToReturnF ?: properties.backUrlFail

    companion object {
        private const val RUB_CURRENCY = "RUB"
        private const val RU_LANGUAGE = "ru"

        @JvmStatic
        @Named("mapAmount")
        fun mapAmount(amount: BigDecimal): Int = GpbRequestMapper.mapRequestAmount(amount)

        @JvmStatic
        @Named("emptyToNull")
        fun emptyToNull(payItems: LinkedHashMap<String, String>): Map<String, String>? = payItems.takeIf { it.isNotEmpty() }

        @JvmStatic
        @Named("trueOrNull")
        fun trueOrNull(value: Boolean?): Boolean? = true.takeIf { value == true }
    }
}
