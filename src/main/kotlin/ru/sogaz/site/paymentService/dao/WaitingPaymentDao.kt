package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

interface WaitingPaymentDao {
    fun findByPaymentBankId(paymentBankId: String): WaitingPayment?

    fun saveWaitingForPayment(payment: Payment): WaitingPayment

    fun findTopNEarliestUpdated(limit: Int): List<WaitingPayment>

    fun deleteByPaymentBankId(paymentBankId: String)

    fun updateTimeByPaymentBankId(paymentBankId: String)
}
