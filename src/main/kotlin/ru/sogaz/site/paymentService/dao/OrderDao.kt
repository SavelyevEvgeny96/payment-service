package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order

interface OrderDao {
    fun getOrderByCode(
        code: String,
        traceId: String,
    ): Order

    fun getOrderId(orderId: String): Order?

    fun save(order: Order)
}
