package ru.sogaz.site.paymentService.dao

interface GetBankPriorityDao {
    fun getBankPriority(traceId: String): String
}
