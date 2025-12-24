package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.rabbit.OrderPaidEvent
import java.util.UUID

interface OrderPaidEventFactory {
    fun success(orderId: UUID?): OrderPaidEvent

    fun error(
        orderId: UUID?,
        errorText: String,
    ): OrderPaidEvent
}
