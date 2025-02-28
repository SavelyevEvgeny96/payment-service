package ru.sogaz.site.paymentService.dto

data class GPBPaymentRequest(
    val portal_id: String,
    val token: String,
    val supported3ds: Boolean,
    val merchantId: String,
    val orderId: String,
    val stateInProgress: String,
    val stateRedirect: String,
    val backUrlS: String,
    val backUrlF: String,
    val lang: String,
    val amount: Double,
    val currency: String,
    val description: String
)