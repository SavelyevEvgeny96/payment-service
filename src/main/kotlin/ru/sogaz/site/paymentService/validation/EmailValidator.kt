package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует формату email.
 */

class EmailValidator(
    private val emailRegex: Regex,
) {
    fun isValid(value: String?): Boolean = value?.matches(emailRegex) ?: false
    fun isValidManager(value: String?):Boolean = value.isNullOrEmpty() || value.matches(emailRegex)

}
