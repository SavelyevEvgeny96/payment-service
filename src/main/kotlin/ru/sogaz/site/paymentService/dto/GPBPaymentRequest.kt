package ru.sogaz.site.paymentService.dto

data class GPBPaymentRequest(
    val returnUrl: String?,
    val portalId: String,
    val token: String,
    val merchantId: String,
    val orderId: String,
    val backUrlS: String,
    val backUrlF: String,
    val amount: String?,
    val currency: String,
    val description: String,
)


