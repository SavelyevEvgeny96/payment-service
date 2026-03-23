package ru.sogaz.site.paymentService.model.v2.event

import java.math.BigDecimal
import java.util.UUID

data class RefundEvent(
    val orderId: UUID,
    val amount: BigDecimal,
    val description: String,
)
