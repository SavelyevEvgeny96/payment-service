package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.PaymentOperationHistory

interface PaymentOperationHistoryDao {
    fun save(paymentOperationHistory: PaymentOperationHistory)
}
