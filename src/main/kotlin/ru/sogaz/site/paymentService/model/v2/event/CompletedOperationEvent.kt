package ru.sogaz.site.paymentService.model.v2.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CompletedOperationEvent(
    @field:JsonProperty
    val paymentId: UUID,
    val orderId: UUID,
    val totalAmount: BigDecimal,
    val depersonalization: Boolean = false,
    val status: String,
    val keyCard: String? = null,
    val bank: String,
    val paymentType: String,
    val payDate: Instant,
    val errorText: String?,
)
