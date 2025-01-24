package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.ExternalSystemCodeValidator

/**
 * Аннотация для валидации поля externalSystemCode.
 * Эта аннотация проверяет, что значение поля соответствует одному из допустимых кодов внешней системы (ADI, FOP, LK, 1C).
 */
@Constraint(validatedBy = [ExternalSystemCodeValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ExternalSystemCodeConstraint(
    val message: String = "Неверное значение externalSystemCode"
)