package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.EmailValidator

/**
 * Аннотация для валидации поля email.
 * Эта аннотация проверяет, что значение поля соответствует формату email.
 */
@Constraint(validatedBy = [EmailValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EmailConstraint(
    val message: String = "Неверный формат email"
)
