package ru.sogaz.site.paymentService.dto.request

data class GpbCallbackRequest(
    val trxId: String,
    val merchId: String?,
    val resultCode: Int?,
    val amount: String?,
    val accountId: String?,
    val orderId: String?,
    val rrn: String?,
    val authCode: String?,
    val srcType: String?,
    val maskedPan: String?,
    val isFullyAuthenticated: String?,
    val transmissionDateTime: String?,
    val discountType: String?,
    val discountAmount: String?,
    val paymentSystem: String?,
    val ts: String?,
    val signature: String,
)
