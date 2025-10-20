package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.WaitingPayment
import java.util.UUID

@Repository
interface WaitingPaymentRepository : JpaRepository<WaitingPayment, UUID> {
    @Query("SELECT cp FROM WaitingPayment cp WHERE cp.paymentBankId = :paymentBankId")
    fun findByPaymentBankId(paymentBankId: String): WaitingPayment?
}
