package ru.sogaz.site.paymentService.service.v2.operation.model

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

/**
 * Объект хранящий результаты операции и запись об операции для передачи по цепочки шагов стратегии
 */
data class StepResult<RESULT>(
    val operation: IdempotentOrderOperation,
    val result: RESULT,
)
