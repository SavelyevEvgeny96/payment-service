package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import java.util.UUID

interface OrderDao {
    fun getOrderId(orderId: String): Order

    fun findById(orderId: UUID): Order?

    fun save(order: Order): Order

    fun renewUpdateDate(order: Order): Order
}
