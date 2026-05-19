package ru.sogaz.site.paymentService.service.v2.operation.inline

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.AbstractOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.FollowingOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.UnitFollowingOperationStrategy

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание первого шага стратегии без маппинга результатов выполнения в запись операции в базе
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.step(
    noinline action: REQUEST.() -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = step(action) { _ -> this }

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание первого шага стратегии с маппингом результатов выполнения в запись операции,
 * но без сохранения промежуточных результатов.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.step(
    noinline action: REQUEST.() -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = OperationStrategy(this, false, action, resultToOrderOperationMapper)

/**
 * Inline функция вызываемая в контексте существующего объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущей.
 * Без маппинга результатов выполнения в запись операции в базе.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.step(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = step(action) { _ -> this }

/**
 * Inline функция вызываемая в контексте существующего объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущей.
 * С маппингом результатов выполнения в запись операции в базе.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.step(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = FollowingOperationStrategy(this.request, false, this, action, resultToOrderOperationMapper)

/**
 * Inline функция вызываемая в контексте существующего объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущей.
 * Без маппинга результатов выполнения в запись операции в базе.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> AbstractOperationStrategy<REQUEST, Unit>.step(
    noinline action: REQUEST.() -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = UnitFollowingOperationStrategy(this.request, true, this, action) { _ -> this }

/**
 * Inline функция вызываемая в контексте существующего объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущей.
 * С маппингом результатов выполнения в запись операции в базе.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> AbstractOperationStrategy<REQUEST, Unit>.step(
    noinline action: REQUEST.() -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> =
    UnitFollowingOperationStrategy(this.request, true, this, action, resultToOrderOperationMapper)

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание первого шага стратегии с маппингом результатов выполнения в запись опреации в базе
 * и с последующим сохранением этих промежуточных результатов
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.stepWithSave(
    noinline action: REQUEST.() -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = OperationStrategy(this, true, action, resultToOrderOperationMapper)

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущего объекта.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.stepWithSave(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = FollowingOperationStrategy(this.request, true, this, action, resultToOrderOperationMapper)

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущего объекта.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> AbstractOperationStrategy<REQUEST, Unit>.stepWithSave(
    noinline action: REQUEST.() -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = UnitFollowingOperationStrategy(this.request, true, this, action) { _ -> this }

/**
 * Inline функция по формированию объекта стратегии.
 * Реализует создание последующего шага стратегии относительно текущего объекта.
 * Позволяет собирать последовательное описание хода выполнения взаимодействия с банком.
 */
inline fun <reified REQUEST : OperationRequest, reified RESULT> AbstractOperationStrategy<REQUEST, Unit>.stepWithSave(
    noinline action: REQUEST.() -> RESULT,
    noinline resultToOrderOperationMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> =
    UnitFollowingOperationStrategy(this.request, true, this, action, resultToOrderOperationMapper)
