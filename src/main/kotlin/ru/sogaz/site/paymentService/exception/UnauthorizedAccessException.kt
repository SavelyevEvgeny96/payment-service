package ru.sogaz.site.paymentService.exception

/**
 * Исключение для ошибок авторизации.
 * Используется, когда токен недействителен или отсутствует.
 */
class UnauthorizedAccessException(message: String) : RuntimeException(message)