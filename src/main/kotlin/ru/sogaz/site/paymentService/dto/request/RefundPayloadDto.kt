package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import java.util.*


@JsonInclude(JsonInclude.Include.NON_NULL)
data class RefundPayloadDto(
    val metaInfo: List<MetaInfoOrder>,
    val orderId: UUID?,
    val routingKeyStatus: String? = null,
)