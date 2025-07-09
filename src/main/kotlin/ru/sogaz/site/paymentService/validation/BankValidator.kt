package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля банка равно "gpb".
 */

class BankValidator {
    fun isValid(value: String?): Boolean = value.isNullOrEmpty() || value == GPB

    companion object {
        const val GPB = "gpb"
    }
}
