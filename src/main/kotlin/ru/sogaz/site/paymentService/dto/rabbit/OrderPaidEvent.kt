package ru.sogaz.site.paymentService.dto.rabbit

import jakarta.validation.constraints.NotNull
import ru.sogaz.site.paymentService.enums.OrderPaidStatus
import java.util.UUID

data class OrderPaidEvent(
    @field:NotNull
    val metaInfo: MetaInfoOrder,
    val orderId: UUID?,
    val status: OrderPaidStatus,
    val errorText: String? = null,
)
