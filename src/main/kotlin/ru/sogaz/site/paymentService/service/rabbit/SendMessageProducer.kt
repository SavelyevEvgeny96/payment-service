package ru.sogaz.site.paymentService.service.rabbit

import ru.sogaz.site.paymentService.dto.request.PaidOrderMessage

interface SendMessageProducer {
    fun sendMessagePaidOrderAndPaymentStatus(
        routingKey: String,
        paidOrderMessage: PaidOrderMessage,
        exchange: String,
    )
}
