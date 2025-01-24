package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.PhoneValidator


/**
 * Аннотация для валидации поля phone.
 * Эта аннотация проверяет, что значение поля соответствует формату телефонного номера.
 */
@Constraint(validatedBy = [PhoneValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PhoneConstraint(
    val message: String = "Неверный формат телефона"
)
