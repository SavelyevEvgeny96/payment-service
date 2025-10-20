package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment

interface CallbackPaymentDao {
    fun save(callbackPayment: CallbackPayment): CallbackPayment

    fun findByPaymentBankId(paymentBankId: String): CallbackPayment?

    fun saveCallbackForPayment(payment: Payment): CallbackPayment
}
