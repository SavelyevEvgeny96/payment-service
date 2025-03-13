package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * isValid Проверяет, что значение поля содержит от 2 до 30 символов.
 * isValidDoc  Проверяет, что значение поля содержит только цифры, пробел
 * isValidCorrectInput Проверяет, что значение поля содержит только русские буквы, пробел и тире"
 */
class PolicyholderValidator(
    private val regex: Regex,
    private val regexDoc: Regex,
) {
    fun isValid(value: String?): Boolean = value.isNullOrBlank() || value.length in 2..30

    fun isValidDoc(value: String?): Boolean = value.isNullOrBlank() || value.matches(regexDoc)

    fun isValidCorrectInput(value: String?): Boolean {
        if (value.isNullOrBlank()) {
            return true
        }
        return value.matches(regex)
    }
}
