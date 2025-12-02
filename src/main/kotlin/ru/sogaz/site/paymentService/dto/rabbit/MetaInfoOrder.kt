package ru.sogaz.site.paymentService.dto.rabbit

import java.time.Instant

data class MetaInfoOrder(
    val eventTimeIso: Instant?,
    val author: String,
    val routingKey: String,
)
