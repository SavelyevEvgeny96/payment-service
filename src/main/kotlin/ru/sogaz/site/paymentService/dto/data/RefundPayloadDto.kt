package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import java.util.UUID

data class RefundPayloadDto(
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID?,
)
