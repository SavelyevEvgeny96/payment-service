package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `PhoneValidator`.
 * Проверяет, что значение поля соответствует формату телефонного номера.
 */
class PhoneValidator {
    fun isValid(value: String?): Boolean = value?.matches(Regex("^(\\+\\d{1,3}[- ]?)?\\d{10}\$")) ?: true
}
