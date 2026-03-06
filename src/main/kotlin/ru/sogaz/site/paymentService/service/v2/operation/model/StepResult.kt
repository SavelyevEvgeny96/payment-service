package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

data class StepResult<RESULT>(
    val operation: IdempotentOrderOperation,
    val result: RESULT,
)
