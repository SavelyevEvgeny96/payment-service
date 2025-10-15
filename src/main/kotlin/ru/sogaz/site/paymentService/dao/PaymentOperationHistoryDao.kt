package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory

interface PaymentOperationHistoryDao {
    fun saveRecordOperationHistory(
        order: Order?,
        traceId: String,
        actionTypeName: String,
    )

    fun save(
        order: Order,
        actionTypeName: String,
    ): PaymentOperationHistory
}
