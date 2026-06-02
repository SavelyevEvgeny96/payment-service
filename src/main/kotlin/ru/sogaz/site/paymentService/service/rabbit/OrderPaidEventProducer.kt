package ru.sogaz.site.paymentService.service.rabbit

import ru.sogaz.site.paymentService.dto.rabbit.OrderPaidEvent

interface OrderPaidEventProducer {
    fun send(event: OrderPaidEvent)
}
