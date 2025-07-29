package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment

interface GetPaymentDao {
    fun getPayment(
        traceId: String,
        order: Order,
    ): Payment?
}
