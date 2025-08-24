package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.PaymentType

interface PaymentTypeDao {
    fun getPaymentType(
        traceId: String,
        type: String,
    ): PaymentType
}
