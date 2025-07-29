package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Bank

interface GetBankDao {
    fun getBank(
        bankId: String?,
        traceId: String,
    ): Bank?
}
