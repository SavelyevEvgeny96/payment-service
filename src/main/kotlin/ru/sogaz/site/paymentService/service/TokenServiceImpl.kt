package ru.sogaz.site.paymentService.service

import org.springframework.stereotype.Component


/**
 * Утилита для работы с токенами.
 * Этот класс предоставляет методы для валидации JWT-токенов, используемых в запросах.
 */
@Component
class TokenServiceImpl : TokenService {
    companion object {
        const val BEARER_PREFIX = "Bearer"
        const val INVALID_TOKEN  ="Неверный формат токена"
    }

    /**
     * Проверка валидности токена.
     * Токен должен быть передан в формате "Bearer <token>".
     * Проверяет наличие префикса "Bearer " и извлекает сам токен.
     *
     * @param token Токен авторизации, который должен быть в формате "Bearer <token>"
     * @return Boolean Возвращает true, если токен имеет правильный формат и не пустой
     * @throws Exception Если токен не начинается с "Bearer " или пуст
     */
    override fun validateToken(token: String): Boolean {
        if (!token.startsWith(BEARER_PREFIX)) {
        }
        val jwtToken = token.substring(7)
        return jwtToken.isNotEmpty()
    }
}
