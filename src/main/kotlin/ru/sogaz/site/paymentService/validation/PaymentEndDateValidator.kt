package ru.sogaz.site.paymentService.validation

/**
 * Валидатор для аннотации `PaymentEndDateConstraint`.
 * Проверяет, что значение поля соответствует формату даты окончания.
 */
class PaymentEndDateValidator {
    fun isValid(value: String?): Boolean {
        val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+0000\$")
        return value?.matches(datePattern) ?: false
    }
}
