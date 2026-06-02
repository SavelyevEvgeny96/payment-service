package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentService.entity.ChequeSent

interface ChequeSentRepository : JpaRepository<ChequeSent, Long> {
    fun findByPaymentBankId(paymentBankId: String): ChequeSent?
}
