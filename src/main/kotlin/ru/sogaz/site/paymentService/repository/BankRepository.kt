package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.Bank
import java.util.*

@Repository
interface BankRepository : JpaRepository<Bank, Long> {
    override fun findById(bankId: Long): Optional<Bank>

    fun findFirstByOrderById(): Bank
}
