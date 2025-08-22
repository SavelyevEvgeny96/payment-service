package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order

interface PaymentOperationHistoryDao {
    fun saveRecordOperationHistory(order: Order?, clientSystem: ClientSystem?, traceId: String, actionTypeName: String)
}
