package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.CallbackPayment

interface CallbackPaymentDao {
    fun save(callbackPayment: CallbackPayment)

    fun findByPaymentBankId(paymentBankId: String): CallbackPayment?
}
