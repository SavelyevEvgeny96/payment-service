package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus

interface OrderDao {
    fun getOrderByCode(
        code: String,
        traceId: String,
    ): Order

    fun getOrderStatus(
        traceId: String,
        stateOrder: String,
    ): OrderStatus
}
