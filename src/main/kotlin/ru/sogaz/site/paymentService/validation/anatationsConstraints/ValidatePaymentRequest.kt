package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.PaymentRequestValidator

/**
 * Аннотация для валидации поля PaymentRequest.
 * Используется для объединения всех проверок на одном уровне.
 */
@Constraint(validatedBy = [PaymentRequestValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ValidatePaymentRequest
