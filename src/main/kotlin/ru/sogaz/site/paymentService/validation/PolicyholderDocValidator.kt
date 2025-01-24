package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.PolicyholderDocConstraint

/**
 * Валидатор для аннотации `PolicyholderDocConstraint`.
 * Проверяет, что значение поля состоит только из цифр и пробелов.
 */
class PolicyholderDocValidator : ConstraintValidator<PolicyholderDocConstraint, String> {

    override fun initialize(constraintAnnotation: PolicyholderDocConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.matches(Regex("^[0-9\\s]+$")) ?: true
    }
}