package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order

interface OrderDao {

    fun getOrderId(
        traceId: String,
        orderId: String,
    ): Order

    fun save(order: Order)
}
