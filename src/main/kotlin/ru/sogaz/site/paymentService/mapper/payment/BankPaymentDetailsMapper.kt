package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.dto.response.AkbOrderStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbQrResult
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.mapper.common.ExtendedCodeMapper

@Mapper(
    componentModel = "spring",
    uses = [
        CardDetailsMapper::class,
        PaymentStatusMapper::class,
        ExtendedCodeMapper::class,
    ],
    imports = [PaymentStatusEnum::class],
)
interface BankPaymentDetailsMapper {
    @Mapping(target = "status", source = "result.status", defaultValue = "WAIT")
    @Mapping(
        target = "extendedCode",
        source = "result.extendedCode",
        qualifiedByName = ["mapExtendedCode"],
    )
    @Mapping(target = "cardDetails", source = "gpbCardDetails")
    fun convert(response: GpbCardPaymentStatusResponse): BankPaymentDetails

    @Mapping(target = "status", source = ".", defaultValue = "WAIT")
    @Mapping(target = "id", source = "trx_id")
    @Mapping(target = "cardDetails", source = ".")
    fun convert(response: GpbCallback): BankPaymentDetails

    fun convert(result: GpbQrResult?): BankPaymentDetails

    fun convert(
        response: AkbOrderStatusResponse,
        status: AkbPaymentStatusEnum,
    ): BankPaymentDetails
}
