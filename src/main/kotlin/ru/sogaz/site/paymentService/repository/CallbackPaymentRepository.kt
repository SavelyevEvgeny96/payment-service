package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.CallbackPayment

@Repository
interface CallbackPaymentRepository : JpaRepository<CallbackPayment, Long> {
    @Query("SELECT cp FROM CallbackPayment cp WHERE cp.paymentBankId = :paymentBankId")
    fun findByPaymentBankId(paymentBankId: String): CallbackPayment?
}
