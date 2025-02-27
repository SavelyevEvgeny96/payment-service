package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля соответствует формату даты окончания.
 */
class PaymentEndDateValidatorFormat(private val codeRegex: Regex) {
    fun isValid(value: String?): Boolean {
        return value?.matches(codeRegex) ?: false
    }
}
