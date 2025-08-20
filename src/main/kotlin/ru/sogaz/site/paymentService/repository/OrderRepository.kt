package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderId(orderId: String): Order
}
