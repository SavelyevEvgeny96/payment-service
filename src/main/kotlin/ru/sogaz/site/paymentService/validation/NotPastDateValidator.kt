package ru.sogaz.site.paymentService.validation

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Валидатор для аннотации `NotPastDateValidator`.
 * Проверяет, что значение поля не является датой в прошлом.
 */
class NotPastDateValidator {
    fun isValid(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+0000")
        val dateTime = LocalDateTime.parse(value, formatter)
        val now = LocalDateTime.now()
        return !dateTime.isBefore(now)
    }
}
