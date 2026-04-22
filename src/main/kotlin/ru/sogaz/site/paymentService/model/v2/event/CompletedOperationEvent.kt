package ru.sogaz.site.paymentService.model.v2.event

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.model.v2.bank.response.ClientCardDetails
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CompletedOperationEvent(
    @field:JsonProperty
    val paymentId: UUID,
    val orderId: UUID,
    val paymentBankId: String,
    val totalAmount: BigDecimal,
    val depersonalization: Boolean = false,
    val status: String,
    val card: ClientCardDetails? = null,
    val bank: String,
    val operationType: OperationType,
    val paymentType: String,
    val payDate: Instant,
    val payerIp: String?,
    val externalErrorCode: String?,
    val errorText: String?,
)
