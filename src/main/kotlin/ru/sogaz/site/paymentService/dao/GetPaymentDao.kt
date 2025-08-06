package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment

interface GetPaymentDao {
    fun getPayment(
        traceId: String,
        paymentId: Long,
    ): Payment?

    fun getPaymentFromBankId(
        bankId: String,
        traceId: String,
    ): Payment?
}
