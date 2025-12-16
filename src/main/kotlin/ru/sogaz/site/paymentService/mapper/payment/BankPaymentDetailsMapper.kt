package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.response.AkbOrderStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbQrResult
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum

@Mapper(
    componentModel = "spring",
    uses = [CardDetailsMapper::class, PaymentStatusMapper::class],
    imports = [PaymentStatusEnum::class],
)
interface BankPaymentDetailsMapper {
    @Mapping(target = "status", source = "result.status", defaultValue = "WAIT")
    @Mapping(target = "cardDetails", source = "gpbCardDetails")
    @Mapping(target = "extendedCode", source = "result.extendedCode")
    fun convert(response: GpbCardPaymentStatusResponse): BankPaymentDetails

    fun convert(result: GpbQrResult?): BankPaymentDetails

    fun convert(
        response: AkbOrderStatusResponse,
        status: AkbPaymentStatusEnum,
    ): BankPaymentDetails
}
