package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import java.util.Optional
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {

    fun findByOrderId(orderId: UUID?): Optional<Payment>

    fun findByPaymentBankId(paymentBankId: String?): Payment

    @Query(
        """
    SELECT p FROM Payment p 
    JOIN p.state s 
    WHERE s IN :statuses
    """,
    )
    fun findByStatuses(
        @Param("statuses") statuses: List<PaymentStatusEnum>,
    ): List<Payment>
}
