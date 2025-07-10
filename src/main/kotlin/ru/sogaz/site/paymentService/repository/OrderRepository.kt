package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.orderStatus.stateId IN :statuses")
    fun findByStatuses(
        @Param("statuses") statuses: List<String>,
    ): List<Order>

    fun findByCode(code: String): Order
}
