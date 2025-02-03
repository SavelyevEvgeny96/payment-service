package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `BankConstraint`.
 * Проверяет, что значение поля банка равно "gpb".
 */
class BankValidator {
    fun isValid(value: String?): Boolean = value == GPB

    companion object {
        const val GPB = "gpb"
    }
}
