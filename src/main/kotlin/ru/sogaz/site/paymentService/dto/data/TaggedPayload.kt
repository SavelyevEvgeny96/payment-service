package ru.sogaz.site.paymentService.dto.data

data class TaggedPayload<T>(
    val tag: Long,
    val dto: T,
)
