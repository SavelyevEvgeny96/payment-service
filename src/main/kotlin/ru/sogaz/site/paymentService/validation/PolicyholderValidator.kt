package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.PolicyholderConstraint

/**
 * Валидатор для аннотации `PolicyholderConstraint`.
 * Проверяет, что значение поля содержит от 2 до 30 символов.
 */
class PolicyholderValidator : ConstraintValidator<PolicyholderConstraint, String> {

    override fun initialize(constraintAnnotation: PolicyholderConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.length in 2..30
    }
}