package ru.sogaz.site.paymentService.service.v2.operation.inline

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.AbstractOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.bankOperationCommand(
    noinline requestToOrderOperationMapper: REQUEST.() -> IdempotentOrderOperation,
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = OperationCommand(this, requestToOrderOperationMapper, strategy)

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.bankOperationCommand(
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = OperationCommand(this, null, strategy)

inline fun <reified REQUEST : OperationRequest, reified RESULT> OperationCommand<REQUEST, RESULT>.onFinalState(
    noinline block: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): OperationCommand<REQUEST, RESULT> = apply { finalStateAction = block }
