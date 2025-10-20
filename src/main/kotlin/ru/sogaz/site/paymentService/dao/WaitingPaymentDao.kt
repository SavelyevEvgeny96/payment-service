package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

interface WaitingPaymentDao {
    fun save(waitingPayment: WaitingPayment): WaitingPayment

    fun findByPaymentBankId(paymentBankId: String): WaitingPayment?

    fun saveWaitingForPayment(payment: Payment): WaitingPayment
}
