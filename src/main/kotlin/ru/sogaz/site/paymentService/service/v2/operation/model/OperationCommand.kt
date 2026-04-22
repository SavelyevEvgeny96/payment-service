package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank

data class OperationCommand<REQUEST, RESULT>(
    val request: REQUEST,
    val bank: OperationBank,
    val strategy: AbstractOperationStrategy<REQUEST, RESULT>,
    val requestMapper: (REQUEST.() -> IdempotentOrderOperation)? = null,
) {
    var finalStateAction: IdempotentOrderOperation.(RESULT) -> Unit = { }
    var onFailureAction: IdempotentOrderOperation.(Throwable) -> Unit = { }
}
