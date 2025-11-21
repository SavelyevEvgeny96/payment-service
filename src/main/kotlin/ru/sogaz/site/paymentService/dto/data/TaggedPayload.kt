package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto

data class TaggedPayload(
    val tag: Long,
    val dto: OrderPayloadDto,
)
