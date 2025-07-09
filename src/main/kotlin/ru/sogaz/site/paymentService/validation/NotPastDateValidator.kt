package ru.sogaz.site.paymentService.validation

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля не является датой в прошлом.
 */
class NotPastDateValidator {
    companion object {
        const val PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX"
    }

    fun isValid(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }

        val formatter = DateTimeFormatter.ofPattern(PATTERN)

        val dateTime = ZonedDateTime.parse(value, formatter)
        val now = ZonedDateTime.now()
        return !dateTime.isBefore(now)
    }
}
