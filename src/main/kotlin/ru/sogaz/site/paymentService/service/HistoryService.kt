package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.entity.Order

interface HistoryService {
    fun createOrderHistoryRecord(
        order: Order,
        traceId: String,
    )
}
