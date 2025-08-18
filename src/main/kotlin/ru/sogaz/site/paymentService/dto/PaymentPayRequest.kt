package ru.sogaz.site.paymentService.dto

data class PaymentPayRequest(
    val orderId: String,
    val urlToReturn: String?,
    val urlToReturnF: String?,
)
