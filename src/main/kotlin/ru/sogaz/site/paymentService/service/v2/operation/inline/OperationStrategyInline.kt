package ru.sogaz.site.paymentService.service.v2.operation.inline

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.AbstractOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.FollowingOperationStrategy
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationStrategy

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.step(
    noinline action: REQUEST.() -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = step(action) { _ -> this }

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.step(
    noinline action: REQUEST.() -> RESULT,
    noinline mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = OperationStrategy(this, false, action, mapResult)

inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.step(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = step(action) { _ -> this }

inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.step(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
    noinline mapResult: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = FollowingOperationStrategy(this.request, false, this, action, mapResult)

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.stepWithSave(
    noinline action: REQUEST.() -> RESULT,
): AbstractOperationStrategy<REQUEST, RESULT> = stepWithSave(action) { _ -> this }

inline fun <reified REQUEST : OperationRequest, reified RESULT> REQUEST.stepWithSave(
    noinline action: REQUEST.() -> RESULT,
    noinline resultMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = OperationStrategy(this, true, action, resultMapper)

inline fun <reified REQUEST : OperationRequest, PREV_RESULT, reified RESULT> AbstractOperationStrategy<REQUEST, PREV_RESULT>.stepWithSave(
    noinline action: REQUEST.(PREV_RESULT) -> RESULT,
    noinline resultMapper: IdempotentOrderOperation.(RESULT) -> IdempotentOrderOperation,
): AbstractOperationStrategy<REQUEST, RESULT> = FollowingOperationStrategy(this.request, true, this, action, resultMapper)
