package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import java.util.UUID

@Repository
interface SubOrderRepository : JpaRepository<SubOrder, UUID> {
    fun findFirstByOrderId(orderId: Order?): SubOrder

    fun findAllByOrderId(orderId: UUID): List<SubOrder>

    fun findAllByOrderId(orderId: Order): List<SubOrder>
}
