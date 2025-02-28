package ru.sogaz.site.paymentService.dto

import jakarta.validation.constraints.NotNull

data class PaymentPayRequest(
    @NotNull
    val code: String,
    val urlToReturn: String,
    val urlToReturnF: String,
    var traceId: String,
)
