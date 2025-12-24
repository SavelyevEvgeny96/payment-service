package ru.sogaz.site.paymentService.model.web.request

import jakarta.validation.constraints.Positive
import ru.sogaz.site.paymentService.validation.constraint.UniqueMainContract
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PayRequest(
    val orderId: UUID,
    @field:UniqueMainContract
    val contractsInfo: List<ContractInfo>,
    @field:Positive
    val amount: BigDecimal,
    val saveCard: Boolean,
)

data class ContractInfo(
    val contractNumber: String,
    val contractDate: Instant,
    val mainContractCheck: Boolean,
)
