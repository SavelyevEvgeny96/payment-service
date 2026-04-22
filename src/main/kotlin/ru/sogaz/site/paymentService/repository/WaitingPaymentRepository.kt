package ru.sogaz.site.paymentService.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.WaitingPayment
import java.util.UUID

@Repository
interface WaitingPaymentRepository : JpaRepository<WaitingPayment, UUID> {
    fun findByPaymentBankId(paymentBankId: String): WaitingPayment?

    fun findAllByOrderByUpdateDate(page: Pageable): List<WaitingPayment>

    @Modifying
    fun deleteByPaymentBankId(paymentBankId: String)

    @Modifying
    @Query(
        """
        UPDATE WaitingPayment wp 
        SET wp.updateDate = CURRENT_TIMESTAMP
        WHERE wp.paymentBankId = :paymentBankId
    """,
    )
    fun updateTimeByPaymentBankId(paymentBankId: String)
}
