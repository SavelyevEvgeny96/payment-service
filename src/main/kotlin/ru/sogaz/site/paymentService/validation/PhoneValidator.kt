package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует формату телефонного номера.
 */
class PhoneValidator(private val codeRegex: Regex) {
    fun isValid(value: String?): Boolean = value?.matches(codeRegex) ?: false
}
