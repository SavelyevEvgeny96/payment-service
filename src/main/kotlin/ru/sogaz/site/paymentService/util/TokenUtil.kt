package ru.sogaz.site.paymentService.util

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.constants.ErrorMessages.INVALID_TOKEN
import ru.sogaz.site.paymentService.exception.UnauthorizedAccessException

/**
 * Утилита для работы с токенами.
 * Этот класс предоставляет методы для валидации JWT-токенов, используемых в запросах.
 */
@Component
class TokenUtil {
    /**
     * Проверка валидности токена.
     * Токен должен быть передан в формате "Bearer <token>".
     * Проверяет наличие префикса "Bearer " и извлекает сам токен.
     *
     * @param token Токен авторизации, который должен быть в формате "Bearer <token>"
     * @return Boolean Возвращает true, если токен имеет правильный формат и не пустой
     * @throws Exception Если токен не начинается с "Bearer " или пуст
     */
    fun validateToken(token: String): Boolean {
        if (!token.startsWith("Bearer ")) {
            throw UnauthorizedAccessException(INVALID_TOKEN)
        }
        val jwtToken = token.substring(7)
        return jwtToken.isNotEmpty()
    }
}
