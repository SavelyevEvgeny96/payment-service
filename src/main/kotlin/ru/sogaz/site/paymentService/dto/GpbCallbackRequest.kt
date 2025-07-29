package ru.sogaz.site.paymentService.dto

data class GpbCallbackRequest(
    val trxId: String,
    val merchId: String,
    val merchantTrx: String?,
    val resultCode: Int,
    val extResultCode: String?,
    val amount: String,
    val accountId: String?,
    val orderId: String,
    val rrn: String?,
    val authCode: String?,
    val srcType: String?,
    val signature: String,
    val rawQueryString: String,
)
