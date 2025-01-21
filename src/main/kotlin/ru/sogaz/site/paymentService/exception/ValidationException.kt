package ru.sogaz.site.paymentService.exception

/**
 * Исключение для ошибок валидации данных.
 * Содержит информацию о поле, где произошла ошибка.
 */
class ValidationException(val field: String, message: String) : RuntimeException(message)