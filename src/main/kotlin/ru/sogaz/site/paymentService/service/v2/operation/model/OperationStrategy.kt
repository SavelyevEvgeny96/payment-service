package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

sealed class AbstractOperationStrategy<REQUEST, RESULT>(
    val request: REQUEST,
    private val needSaveResult: Boolean,
) {
    abstract fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): StepResult<RESULT>

    protected fun <T> StepResult<T>.saveResult(operationSaver: IdempotentOrderOperation.() -> Unit): StepResult<T> =
        when (needSaveResult) {
            true -> apply { operation.operationSaver() }
            false -> this
        }
}

class OperationStrategy<REQUEST, RESULT>(
    request: REQUEST,
    needSaveResult: Boolean,
    private val action: REQUEST.() -> RESULT,
    private val mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
) : AbstractOperationStrategy<REQUEST, RESULT>(request, needSaveResult) {
    override fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): StepResult<RESULT> {
        val result = request.action()
        val updatedOperation = idempotentOrderOperation.mapResult(result)
        return StepResult(updatedOperation, result)
            .saveResult(operationSaver)
    }
}

class FollowingOperationStrategy<REQUEST, PREV_RESULT, RESULT>(
    request: REQUEST,
    needSaveResult: Boolean,
    private val prevOperation: AbstractOperationStrategy<REQUEST, PREV_RESULT>,
    private val action: REQUEST.(PREV_RESULT) -> RESULT,
    private val mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
) : AbstractOperationStrategy<REQUEST, RESULT>(request, needSaveResult) {
    override fun execute(
        idempotentOrderOperation: IdempotentOrderOperation,
        operationSaver: IdempotentOrderOperation.() -> Unit,
    ): StepResult<RESULT> {
        val prevOperationResult = prevOperation.execute(idempotentOrderOperation, operationSaver)
        val result = request.action(prevOperationResult.result)
        return StepResult(prevOperationResult.operation.mapResult(result), result)
            .saveResult(operationSaver)
    }
}
