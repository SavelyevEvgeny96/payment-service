package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.PaymentEndDateConstraint


/**
 * Валидатор для аннотации `PaymentEndDateConstraint`.
 * Проверяет, что значение поля соответствует формату даты окончания.
 */
class PaymentEndDateValidator : ConstraintValidator<PaymentEndDateConstraint, String> {

    override fun initialize(constraintAnnotation: PaymentEndDateConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+0000\$") // проверка формата
        return value?.matches(datePattern) ?: false
    }
}