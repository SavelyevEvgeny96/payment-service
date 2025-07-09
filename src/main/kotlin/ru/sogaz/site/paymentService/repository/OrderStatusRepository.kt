package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.OrderStatus

@Repository
interface OrderStatusRepository : JpaRepository<OrderStatus, Long> {
    fun findByStateId(stateId: String): OrderStatus
}
