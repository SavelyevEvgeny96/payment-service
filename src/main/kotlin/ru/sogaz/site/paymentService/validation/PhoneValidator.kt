package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.PhoneConstraint

/**
 * Валидатор для аннотации `PhoneConstraint`.
 * Проверяет, что значение поля соответствует формату телефонного номера.
 */
class PhoneValidator : ConstraintValidator<PhoneConstraint, String> {

    override fun initialize(constraintAnnotation: PhoneConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.matches(Regex("^(\\+\\d{1,3}[- ]?)?\\d{10}\$")) ?: true
    }
}