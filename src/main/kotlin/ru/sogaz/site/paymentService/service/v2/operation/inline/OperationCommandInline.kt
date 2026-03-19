package ru.sogaz.site.paymentService.service.v2.operation.inline

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.AbstractOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.bankOperationCommand(
    bank: OperationBank,
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = OperationCommand(this, bank, strategy)

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.gpbOperationCommand(
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = bankOperationCommand(OperationBank.GPB, strategy)

inline infix fun <reified REQUEST : OperationRequest, reified RESULT> OperationCommand<REQUEST, RESULT>.onFinalState(
    noinline block: IdempotentOrderOperation.(RESULT) -> Unit,
): OperationCommand<REQUEST, RESULT> = apply { finalStateAction = block }

inline infix fun <reified REQUEST : OperationRequest, reified RESULT> OperationCommand<REQUEST, RESULT>.onFailure(
    noinline block: IdempotentOrderOperation.(Throwable) -> Unit,
): OperationCommand<REQUEST, RESULT> = apply { onFailureAction = block }
