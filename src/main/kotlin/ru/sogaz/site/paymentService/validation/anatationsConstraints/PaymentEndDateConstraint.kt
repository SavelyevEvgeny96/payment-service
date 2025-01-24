package ru.sogaz.site.paymentService.validation.anatationsConstraints

import jakarta.validation.Constraint
import ru.sogaz.site.paymentService.validation.PaymentEndDateValidator

/**
 * Аннотация для валидации поля paymentEndDate.
 * Эта аннотация проверяет, что значение поля соответствует формату даты окончания.
 */
@Constraint(validatedBy = [PaymentEndDateValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PaymentEndDateConstraint(
    val message: String = "Неверный формат даты окончания"
)