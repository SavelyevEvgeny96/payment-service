package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

interface PaymentStatusService {
    fun updateStatus(paymentBankId: String): Payment?

    fun updateStatus(callbackPayment: CallbackPayment): Payment?

    fun updateStatus(waitingPayment: WaitingPayment): Payment?
}
