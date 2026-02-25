package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest

data class OperationCommand<REQUEST : OperationRequest, RESULT>(
    val request: REQUEST,
    val requestToOrderOperationMapper: (REQUEST.() -> IdempotentOrderOperation)? = null,
    val strategy: AbstractOperationStrategy<REQUEST, RESULT>,
) {
    constructor(request: REQUEST, strategy: AbstractOperationStrategy<REQUEST, RESULT>) : this(request, null, strategy)
}
