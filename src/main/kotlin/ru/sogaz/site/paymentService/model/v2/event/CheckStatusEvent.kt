package ru.sogaz.site.paymentService.model.v2.event

import java.util.UUID

data class CheckStatusEvent(
    val operationId: UUID,
)
