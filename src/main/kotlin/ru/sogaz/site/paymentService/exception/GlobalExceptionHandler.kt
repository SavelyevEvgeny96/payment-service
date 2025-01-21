package ru.sogaz.site.paymentService.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Глобальный обработчик ошибок для всего приложения.
 * Перехватывает кастомные исключения и возвращает их в ответах.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Обработчик ошибок валидации.
     * Возвращает структурированный ответ с ошибкой валидации.
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<Map<String, String?>> {
        val errorDetails = mapOf(
            "field" to ex.field,
            "message" to ex.message
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    /**
     * Обработчик ошибок авторизации.
     * Возвращает ошибку с кодом 401 в случае несанкционированного доступа.
     */
    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedAccessException(ex: UnauthorizedAccessException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.UNAUTHORIZED)
    }
}