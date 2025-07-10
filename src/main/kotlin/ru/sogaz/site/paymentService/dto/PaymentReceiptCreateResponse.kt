package ru.sogaz.site.paymentService.dto

data class PaymentReceiptCreateResponse(
    val state: String,
    val externalId: String,
)
