package ru.sogaz.site.paymentService.validation

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Валидатор для аннотации `@ValidatePaymentRequest`.
 * Проверяет, что значение поля не является датой в прошлом.
 */
class NotPastDateValidator {

    fun isValid(value: String): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

        val dateTime = ZonedDateTime.parse(value, formatter)
        val now = ZonedDateTime.now()
        return !dateTime.isBefore(now)
    }
}

