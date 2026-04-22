package ru.sogaz.site.paymentService.mapper.v2.bank.abr.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPreparePushTranResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrOrderResponse

@Mapper
interface AbrSbpResponseMapper {
    @Mapping(target = "paymentBankId", expression = "java( String.valueOf(response.getOrder().getId()) )")
    @Mapping(target = "paymentPageUrl", source = "order.hppUrl")
    @Mapping(target = "bank", constant = "AKB_RUS")
    fun toBankPaymentPageData(response: AbrOrderResponse): BankPaymentPageData

    fun toSbpPaymentPageData(paymentData: BankPaymentPageData, response: AbrPreparePushTranResponse): BankPaymentPageData =
        paymentData.copy(paymentPageUrl = response.getQrcPayload("ipsRu") ?: paymentData.paymentPageUrl)
}
