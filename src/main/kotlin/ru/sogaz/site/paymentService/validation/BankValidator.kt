package ru.sogaz.site.paymentService.validation

import ru.sogaz.site.paymentService.enums.BankEnum

/**
 * Проверяет, что значение поля банка равно "gpb".
 */

class BankValidator {
    fun isValid(value: String?): Boolean = value.isNullOrEmpty() || BankEnum.exists(value)
}
