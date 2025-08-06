package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.PaymentStatus

interface GetPaymentStatusDao {
    fun getPaymentStatus(
        traceId: String,
        status: String,
    ): PaymentStatus
}
