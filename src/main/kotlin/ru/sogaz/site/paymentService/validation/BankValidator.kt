package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ru.sogaz.site.paymentService.validation.anatationsConstraints.BankConstraint


/**
 * Валидатор для аннотации `BankConstraint`.
 * Проверяет, что значение поля банка равно "gpb".
 */
class BankValidator : ConstraintValidator<BankConstraint, String> {

    override fun initialize(constraintAnnotation: BankConstraint?) {}

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value == "gpb"
    }
}