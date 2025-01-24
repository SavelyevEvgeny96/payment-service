package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.PolicyholderValidator

/**
 * Аннотация для валидации поля policyholder.
 * Эта аннотация проверяет, что значение поля содержит от 2 до 30 символов.
 */
@Constraint(validatedBy = [PolicyholderValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PolicyholderConstraint(
    val message: String = "Имя страхователя должно быть от 2 до 30 символов"
)
