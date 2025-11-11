package ru.sogaz.site.paymentService.dto.rabbit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.paymentService.validation.constraint.Email
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
    val recurrent: Boolean,
    @field:NotNull
    val orderId: UUID,
    @field:NotNull
    @field:DecimalMin("0.01")
    val premiumAmount: BigDecimal,
    val saveCard: Boolean?,
    val keyCard: String?,
    @field:Email
    val recipientEmail: String,
    @field:NotBlank
    val recipientPhone: String,
    @field:NotNull
    val dateCreate: Instant,
    @field:NotNull
    val dateEnd: Instant,
)
