package ru.sogaz.site.paymentService.dto

/**
 * DTO для ответа с данными о платеже.
 *
 * @property status Статус обработки платежа
 * @property code Уникальный код платежа
 * @property url Ссылка на оплату
 */
data class PaymentResponse(
    val status: String,
    val code: String,
    val url: String
)