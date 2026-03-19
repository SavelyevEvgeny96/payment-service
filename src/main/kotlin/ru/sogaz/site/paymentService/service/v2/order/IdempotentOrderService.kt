package ru.sogaz.site.paymentService.service.v2.order

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest

interface IdempotentOrderService {
    fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation

    fun <R : PayOperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation

    fun saveOperation(operationRequest: RefundOperationRequest): IdempotentOrderOperation

    fun findOperation(refundRequest: RefundOperationRequest): IdempotentOrderOperation?

    fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        mapToIdempotentOrderOperation: R.() -> IdempotentOrderOperation,
    ): IdempotentOrderOperation

    fun saveOperation(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
