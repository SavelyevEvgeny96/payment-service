package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `ExternalSystemCodeValidator`.
 * Проверяет, что значение поля соответствует одному из допустимых кодов внешней системы.
 */
class ExternalSystemCodeValidator {
    fun isValid(value: String?): Boolean = value?.matches(Regex("^(ADI|FOP|LK|1C)\$")) ?: false
}
