package ru.sogaz.site.paymentService.dto.response.bank

data class GPBRefundResponseDto(
    val status: String,
    val action: String,
    val rrn: String,
    val approvalCode: String,
    val refundToken: String,
)
