package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.OrderStatus

interface GetOrderStatusDao {
    fun getOrderStatus(traceId: String, stateOrder: String): OrderStatus
}