package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.response.AkbOrderStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum

@Mapper(uses = [CardDetailsMapper::class, PaymentStatusMapper::class])
interface BankPaymentDetailsMapper {
    fun convert(
        response: GpbCardPaymentStatusResponse,
        status: StatusEnum,
    ): BankPaymentDetails

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardDetails", ignore = true)
    fun convert(
        response: GpbSbpPaymentStatusResponse,
        status: StatusEnum,
    ): BankPaymentDetails

    @Mapping(target = "cardDetails", ignore = true)
    fun convert(
        response: AkbOrderStatusResponse,
        status: AkbPaymentStatusEnum,
    ): BankPaymentDetails
}
