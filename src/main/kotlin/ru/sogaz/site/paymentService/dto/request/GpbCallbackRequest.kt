package ru.sogaz.site.paymentService.dto.request

data class GpbCallbackRequest(
    val trxId: String,
    val signature: String,
)
