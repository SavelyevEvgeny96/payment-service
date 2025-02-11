package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * isValid Проверяет, что значение поля содержит от 2 до 30 символов.
 * isValidDoc  Проверяет, что значение поля содержит только цифры, пробел
 * isValidCorrectInput Проверяет, что значение поля содержит только русские буквы, пробел и тире"
 */
class PolicyholderValidator {
    fun isValid(value: String?): Boolean = !value.isNullOrBlank() && value?.length in 2..30

    fun isValidDoc(value: String?): Boolean = value?.matches(Regex("^[0-9\\s]+$")) ?: true

    fun isValidCorrectInput(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }

        val regex = Regex("^[а-яА-ЯёЁ\\s-]+$")

        return value.matches(regex)
    }
}
