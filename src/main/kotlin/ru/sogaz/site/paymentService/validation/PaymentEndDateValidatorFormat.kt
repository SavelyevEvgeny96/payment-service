package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует формату даты окончания.
 */
class PaymentEndDateValidatorFormat {
    fun isValid(value: String?): Boolean {
        val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+03:00\$")
        return value?.matches(datePattern) ?: false
    }
}
