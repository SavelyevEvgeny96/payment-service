package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment

interface PaymentDao {
    fun getPayment(
        traceId: String,
        paymentId: Long,
    ): Payment?

    fun getPaymentFromBankId(bankId: String): Payment

    fun save(payment: Payment): Long?

    fun findByPaymentBankId(paymentId: String): Payment

    fun paymentUpdate(
        paymentId: Long?,
        paymentPageUrl: String,
        qtcId: String,
    )
}
