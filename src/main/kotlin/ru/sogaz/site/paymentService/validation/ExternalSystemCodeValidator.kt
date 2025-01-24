package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.ExternalSystemCodeConstraint

/**
 * Валидатор для аннотации `ExternalSystemCodeConstraint`.
 * Проверяет, что значение поля соответствует одному из допустимых кодов внешней системы.
 */
class ExternalSystemCodeValidator : ConstraintValidator<ExternalSystemCodeConstraint, String> {
    override fun initialize(constraintAnnotation: ExternalSystemCodeConstraint?) {}
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.matches(Regex("^(ADI|FOP|LK|1C)\$")) ?: false
    }
}