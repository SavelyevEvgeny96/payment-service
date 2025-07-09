package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.PaymentStatus

@Repository
interface PaymentStatusRepository : JpaRepository<PaymentStatus, Long> {
    fun findByStateId(stateId: String): PaymentStatus
}
