package ru.sogaz.site.paymentService.service

interface TokenService {
    fun validateToken(token: String): Boolean
}