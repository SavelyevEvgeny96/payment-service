package ru.sogaz.site.paymentService.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.FutureOrPresent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class UpdatePaymentInvoiceRequest(
    val orderId: UUID,
    val policyNumber: String?,
    val premiumAmount: BigDecimal?,
    @field:Email
    val recipientEmail: String?,
    @field:FutureOrPresent
    val paymentEndDate: Instant?,
    val cancelCheck: Boolean?,
)
