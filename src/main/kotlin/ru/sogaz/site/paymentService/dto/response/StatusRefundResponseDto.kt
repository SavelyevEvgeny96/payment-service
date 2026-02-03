package ru.sogaz.site.paymentService.dto.response

import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import java.util.UUID

data class StatusRefundResponseDto(
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID?,
    val status: String,
    val errorText: String,
)
