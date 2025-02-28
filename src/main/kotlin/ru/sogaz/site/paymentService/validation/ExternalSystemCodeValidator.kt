package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует одному из допустимых кодов внешней системы.
 */

class ExternalSystemCodeValidator(
    private val codeRegex: Regex,
) {
    fun isValid(value: String?): Boolean = value?.matches(codeRegex) ?: false
}
