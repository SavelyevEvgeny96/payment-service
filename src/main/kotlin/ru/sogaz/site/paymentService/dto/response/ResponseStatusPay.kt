package ru.sogaz.site.paymentService.dto.response

data class ResponseStatusPay(
    val paymentStatus: String,
    val cheque: Boolean,
)
