package ru.sogaz.site.paymentService.service.rabbit

interface SendMessageProducer {
    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    )
}
