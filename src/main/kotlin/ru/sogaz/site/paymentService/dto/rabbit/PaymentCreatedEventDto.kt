package ru.sogaz.site.paymentService.dto.rabbit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCreatedEventDto(
    @field:NotBlank
    val eventType: String,
    @field:NotNull
    val timestamp: Instant,
    @field:NotNull
    val data: PaymentCreatedDataDto,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCreatedDataDto(
    val orderId: UUID?,
    val premiumAmount: BigDecimal?,
    val keyCard: String?,
    val recipientEmail: String?,
    val recipientPhone: String?,
    val dateCreate: String?,
    val dateEnd: String?,
    val bank: String?,
    val paymentType: String?,
)
