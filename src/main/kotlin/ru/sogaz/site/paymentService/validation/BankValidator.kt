package ru.sogaz.site.paymentService.validation

import jakarta.validation.ConstraintValidatorContext


/**
 * Валидатор для аннотации `BankConstraint`.
 * Проверяет, что значение поля банка равно "gpb".
 */
class BankValidator  {
    fun isValid(value: String?): Boolean {
        return value == "gpb"
    }
}