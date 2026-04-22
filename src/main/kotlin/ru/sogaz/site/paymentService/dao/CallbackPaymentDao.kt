package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment

interface CallbackPaymentDao {
    fun save(callbackPayment: CallbackPayment): CallbackPayment

    fun saveCallbackForPayment(payment: Payment): CallbackPayment

    fun findByPaymentBankId(paymentBankId: String): CallbackPayment?

    fun findLimitEarliestUpdated(limit: Int): List<CallbackPayment>

    fun deleteByPaymentBankId(paymentBankId: String)

    fun updateTimeByPaymentBankId(paymentBankId: String)
}
