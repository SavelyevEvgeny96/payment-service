package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusRefundResponseDto(
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID?,
    val status: String,
    val errorText: String?,
)
