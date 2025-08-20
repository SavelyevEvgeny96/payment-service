package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment

interface PaymentDao {
    fun getPayment(
        traceId: String,
        paymentId: Long,
    ): Payment?

    fun getPaymentFromBankId(bankId: String): Payment

    fun save(payment: Payment)

    fun findByPaymentBankId(paymentId: String): Payment
}
