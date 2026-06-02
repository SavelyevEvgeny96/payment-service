package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

@Mapper
interface PaymentBankInfoMapper {
    fun convert(payment: Payment): PaymentBankInfo

    fun convert(callbackPayment: CallbackPayment): PaymentBankInfo

    fun convert(waitingPayment: WaitingPayment): PaymentBankInfo
}
