package ru.sogaz.site.paymentService.service.v2.order

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

interface IdempotentOrderService {
    fun <R : OperationRequest> saveOperation(operationRequest: R): IdempotentOrderOperation

    fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        mapToIdempotentOrderOperation: R.() -> IdempotentOrderOperation,
    ): IdempotentOrderOperation

    fun saveOperation(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
