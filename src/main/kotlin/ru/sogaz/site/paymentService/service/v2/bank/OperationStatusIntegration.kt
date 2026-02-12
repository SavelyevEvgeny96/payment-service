package ru.sogaz.site.paymentService.service.v2.bank

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

interface OperationStatusIntegration {
    fun getOperationState(idempotentOrderOperation: IdempotentOrderOperation): OperationState
}
