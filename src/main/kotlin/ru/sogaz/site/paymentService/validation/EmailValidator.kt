package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.EmailConstraint

/**
 * Валидатор для аннотации `EmailConstraint`.
 * Проверяет, что значение поля соответствует формату email.
 */
class EmailValidator : ConstraintValidator<EmailConstraint, String> {

    override fun initialize(constraintAnnotation: EmailConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$")) ?: true
    }
}