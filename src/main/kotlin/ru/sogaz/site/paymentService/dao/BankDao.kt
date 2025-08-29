package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Bank

interface BankDao {
    fun getBank(
        bankId: String?,
        traceId: String,
        checkBankPriority: String?,
    ): Bank?
}
