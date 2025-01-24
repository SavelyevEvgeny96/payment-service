package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.PolicyholderDocValidator


/**
 * Аннотация для валидации поля policyholderDoc.
 * Эта аннотация проверяет, что значение поля состоит только из цифр и пробелов.
 */
@Constraint(validatedBy = [PolicyholderDocValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PolicyholderDocConstraint(
    val message: String = "Неверный формат документа страхователя"
)