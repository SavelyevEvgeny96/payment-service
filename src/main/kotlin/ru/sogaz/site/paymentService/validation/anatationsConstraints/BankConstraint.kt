package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.BankValidator


/**
 * Аннотация для валидации поля bank.
 * Эта аннотация проверяет, что значение поля должно быть "gpb".
 */
@Constraint(validatedBy = [BankValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class BankConstraint(
    val message: String = "Неверное значение банка, должно быть 'gpb'",
)