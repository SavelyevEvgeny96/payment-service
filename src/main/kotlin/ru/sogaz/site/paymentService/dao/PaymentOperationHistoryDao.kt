package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum

interface PaymentOperationHistoryDao {
    fun saveRecordOperationHistory(
        order: Order?,
        externalSystemCodeEnum: ExternalSystemCodeEnum?,
        traceId: String,
        actionTypeName: String,
    )

    fun save(
        order: Order,
        externalSystemCodeEnum: ExternalSystemCodeEnum?,
        actionTypeName: String,
    ): PaymentOperationHistory
}
