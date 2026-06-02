package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.context.annotation.Profile
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAccountData
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAutoPayHeaders
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GpbSbpAdminAutoPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import java.math.BigDecimal

@Mapper
@Profile(value = ["local", "test", "stage"])
abstract class GpbSbpAdminAutoPayRequestMapper {
    companion object {
        @JvmStatic
        @Named("toRequestAmount")
        fun toRequestAmount(amount: BigDecimal): Int =
            amount
                .movePointRight(2)
                .intValueExact()
    }

    abstract fun toAdminRequest(
        sbpPayOperationRequest: SbpPayOperationRequest,
        headers: GpbSbpAutoPayHeaders,
    ): GpbSbpAdminAutoPayOperationRequest

    @Mapping(target = "amount", qualifiedByName = ["toRequestAmount"])
    @Mapping(target = "account", source = "gpbSbpAccountData.paymentAccount")
    @Mapping(target = "merchantId", source = "gpbSbpAccountData.merchantIdSbpGpb")
    @Mapping(target = "callbackMerchantNotifications", source = "gpbSbpAccountData.callbackUrlSbp")
    @Mapping(target = "paymentPurpose", source = "sbpPayOperationRequest.description")
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "templateVersion", constant = "01")
    @Mapping(target = "qrTtl", constant = "60")
    @Mapping(target = "qrcType", constant = "02")
    abstract fun toSbpRequest(
        sbpPayOperationRequest: GpbSbpAdminAutoPayOperationRequest,
        gpbSbpAccountData: GpbSbpAccountData,
    ): GpbSbpPayRequest
}
