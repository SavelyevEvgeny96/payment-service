package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.enums.BankEnum

interface BankDao {
    fun getBank(
        bankId: String?,
        traceId: String,
        checkBankPriority: String?,
    ): Bank?

    fun resolveBank(currentBank: BankEnum?): BankEnum

    fun findByBankId(bankId: String?): Bank
}
