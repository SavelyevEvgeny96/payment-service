package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

sealed class AbstractOperationStrategy<REQUEST : OperationRequest, RESULT>(
    val request: REQUEST,
    private val needSaveResult: Boolean,
) {
    abstract fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): OperationResult<RESULT>

    protected fun <T> OperationResult<T>.saveResult(operationSaver: IdempotentOrderOperation.() -> Unit): OperationResult<T> =
        when (needSaveResult) {
            true -> apply { operation.operationSaver() }
            false -> this
        }
}

class OperationStrategy<REQUEST : OperationRequest, RESULT>(
    request: REQUEST,
    needSaveResult: Boolean,
    private val action: REQUEST.() -> RESULT,
    private val mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
) : AbstractOperationStrategy<REQUEST, RESULT>(request, needSaveResult) {
    override fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): OperationResult<RESULT> {
        val result = request.action()
        val updatedOperation = idempotentOrderOperation.mapResult(result)
        return OperationResult(updatedOperation, result)
            .saveResult(operationSaver)
    }
}

class FollowingOperationStrategy<REQUEST : OperationRequest, PREV_RESULT, RESULT>(
    request: REQUEST,
    needSaveResult: Boolean,
    private val prevOperation: AbstractOperationStrategy<REQUEST, PREV_RESULT>,
    private val action: REQUEST.(PREV_RESULT) -> RESULT,
    private val mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
) : AbstractOperationStrategy<REQUEST, RESULT>(request, needSaveResult) {
    override fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): OperationResult<RESULT> {
        val prevOperationResult = prevOperation.execute(idempotentOrderOperation, operationSaver)
        val result = request.action(prevOperationResult.result)
        return OperationResult(prevOperationResult.operation.mapResult(result), result)
            .saveResult(operationSaver)
    }
}
