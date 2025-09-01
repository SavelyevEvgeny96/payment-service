package ru.sogaz.site.paymentService.dto.request

data class GPBPaymentRequest(
    val state: State?,
    val portalId: String,
    val token: String,
    val merchantId: String,
    val orderId: String,
    val backUrlS: String,
    val backUrlF: String,
    val amount: String?,
    val currency: String,
    val description: String?,
)

data class State(
    val redirect: String?,
)
