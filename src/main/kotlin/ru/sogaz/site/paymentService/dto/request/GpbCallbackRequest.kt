package ru.sogaz.site.paymentService.dto.request

data class GpbCallbackRequest(
    val trxId: String,
    val merchId: String? = null,
    val resultCode: Int? = null,
    val extResultCode: String? = null,
    val amount: String? = null,
    val accountId: String? = null,
    val orderId: String? = null,
    val rrn: String? = null,
    val authCode: String? = null,
    val srcType: String? = null,
    val maskedPan: String? = null,
    val isFullyAuthenticated: String? = null,
    val transmissionDateTime: String? = null,
    val discountType: String? = null,
    val discountAmount: String? = null,
    val paymentSystem: String? = null,
    val issuerName: String? = null,
    val ts: String? = null,
    val signature: String,
)
