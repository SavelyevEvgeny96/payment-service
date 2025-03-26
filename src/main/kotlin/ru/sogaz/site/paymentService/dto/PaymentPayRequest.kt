package ru.sogaz.site.paymentService.dto

data class PaymentPayRequest(
    val code: String,
    val urlToReturn: String,
    val urlToReturnF: String,
    var traceId: String?,
)
