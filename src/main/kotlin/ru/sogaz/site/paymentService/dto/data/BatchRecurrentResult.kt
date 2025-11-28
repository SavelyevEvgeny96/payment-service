package ru.sogaz.site.paymentService.dto.data

import java.util.UUID

data class BatchRecurrentResult(
    val paid: List<UUID?>,
    val unpaid: List<UUID?>,
)
