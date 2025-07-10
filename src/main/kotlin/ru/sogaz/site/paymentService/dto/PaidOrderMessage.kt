package ru.sogaz.site.paymentService.dto

import java.time.LocalDateTime

data class PaidOrderMessage(
    val orderId: Long,
    val orderCode: String,
    val paymentDate: LocalDateTime,
    val recipientEmail: String?,
    val recipientPhone: String?,
    val traceId: String,
)
