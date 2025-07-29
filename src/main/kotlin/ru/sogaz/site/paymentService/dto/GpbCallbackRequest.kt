package ru.sogaz.site.paymentService.dto

data class GpbCallbackRequest(
    val trxId: String,
    val signature: String,
)
