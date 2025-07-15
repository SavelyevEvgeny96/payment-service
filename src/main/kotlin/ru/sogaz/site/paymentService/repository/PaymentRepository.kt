package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: Order): Payment

    fun findByPaymentBankId(paymentBankId: String): Payment?

    @Query("SELECT o FROM Payment o WHERE o.stateId IN :statuses")
    fun findByStatuses(
        @Param("statuses") statuses: List<String>,
    ): List<Payment>
}
