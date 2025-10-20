package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory

interface PaymentOperationHistoryDao {
    fun saveRecordOperationHistory(
        order: Order?,
        traceId: String,
        actionTypeName: String,
    )

    fun saveForOrder(
        order: Order,
        actionTypeName: String,
    ): PaymentOperationHistory
}
