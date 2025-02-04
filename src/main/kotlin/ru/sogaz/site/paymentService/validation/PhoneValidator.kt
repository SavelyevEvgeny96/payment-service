package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует формату телефонного номера.
 */
class PhoneValidator {
    fun isValid(value: String?): Boolean = value?.matches(Regex("^(\\+|[1-9])\\d{10,11}\$")) ?: false
}
