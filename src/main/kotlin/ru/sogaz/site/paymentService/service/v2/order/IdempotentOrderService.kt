package ru.sogaz.site.paymentService.service.v2.order

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest

interface IdempotentOrderService {
    fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation

    fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
        operationMapper: (R.() -> IdempotentOrderOperation)?,
    ): IdempotentOrderOperation

    fun findOperation(refundRequest: ReversalOperationRequest): IdempotentOrderOperation?

    fun saveOperation(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
