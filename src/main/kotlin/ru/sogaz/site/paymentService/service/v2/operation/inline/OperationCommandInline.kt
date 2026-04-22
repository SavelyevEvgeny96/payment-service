package ru.sogaz.site.paymentService.service.v2.operation.inline

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.AbstractOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand

/**
 * Inline функция по созданию объекта команды.
 * Заполняет объект переданными параметрами и запросом в контексте которого была вызвана функция.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.bankOperationCommand(
    bank: OperationBank,
    noinline requestToOperationMapper: REQUEST.() -> IdempotentOrderOperation,
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = OperationCommand(this, bank, strategy, requestToOperationMapper)

/**
 * Inline функция по созданию объекта команды.
 * Заполняет объект переданными параметрами и запросом в контексте которого была вызвана функция.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.bankOperationCommand(
    bank: OperationBank,
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = OperationCommand(this, bank, strategy)

/**
 * Inline функция по созданию объекта команды.
 * Создает объект с указанной стратегией и банком OperationBank.GPB
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.gpbOperationCommand(
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = bankOperationCommand(OperationBank.GPB, strategy)

/**
 * Inline функция по созданию объекта команды.
 * Заполняет объект банком OperationBank.GPB, переданными параметрами и запросом в контексте которого была вызвана функция.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.gpbOperationCommand(
    noinline requestToOperationMapper: REQUEST.() -> IdempotentOrderOperation,
    strategy: AbstractOperationStrategy<REQUEST, RESULT>,
): OperationCommand<REQUEST, RESULT> = bankOperationCommand(OperationBank.GPB, requestToOperationMapper, strategy)

/**
 * Inline функция вызываемая в контексте существующего объекта команды.
 * Добавляет шаг выполняемый по завершению операции и в том случае, если операция в финальном статусе
 */
inline infix fun <reified REQUEST : OperationRequest, reified RESULT> OperationCommand<REQUEST, RESULT>.onFinalState(
    noinline block: IdempotentOrderOperation.(RESULT) -> Unit,
): OperationCommand<REQUEST, RESULT> = apply { finalStateAction = block }

/**
 * Inline функция вызываемая в контексте существующего объекта команды.
 * Добавляет шаг выполняемый в случае возбуждения ошибки при выполнении команды.
 */
inline infix fun <reified REQUEST : OperationRequest, reified RESULT> OperationCommand<REQUEST, RESULT>.onFailure(
    noinline block: IdempotentOrderOperation.(Throwable) -> Unit,
): OperationCommand<REQUEST, RESULT> = apply { onFailureAction = block }
