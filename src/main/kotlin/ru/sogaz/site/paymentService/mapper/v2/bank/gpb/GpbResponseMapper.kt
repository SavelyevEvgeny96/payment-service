package ru.sogaz.site.paymentService.mapper.v2.bank.gpb

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbQrResult
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.mapper.payment.CardDetailsMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentStatusMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse

@Mapper(
    uses = [CardDetailsMapper::class, PaymentStatusMapper::class],
    imports = [PaymentStatusEnum::class],
)
interface GpbResponseMapper {
    @Mapping(target = "id", source = "token")
    @Mapping(target = "paymentPageUrl", source = "options.paymentPageUrl")
    fun toBankPayData(response: GpbPayCardResponse): BankPaymentPageData

    @Mapping(target = "status", source = "result.status", defaultValue = "WAIT")
    @Mapping(target = "cardDetails", source = "gpbCardDetails")
    fun toBankPaymentDetails(response: GpbCardPaymentStatusResponse): BankPaymentDetails

    fun toBankPaymentDetails(result: GpbQrResult?): BankPaymentDetails
}
