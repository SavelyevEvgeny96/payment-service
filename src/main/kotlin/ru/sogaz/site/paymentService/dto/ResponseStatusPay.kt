package ru.sogaz.site.paymentService.dto

data class ResponseStatusPay(
    val paymentStatus: String,
    val cheque: Boolean,
)
