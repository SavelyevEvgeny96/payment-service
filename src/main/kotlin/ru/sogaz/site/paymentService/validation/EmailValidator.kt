package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `EmailValidator`.
 * Проверяет, что значение поля соответствует формату email.
 */
class EmailValidator {
    fun isValid(value: String?): Boolean = value?.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$")) ?: true
}
