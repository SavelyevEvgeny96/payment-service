package ru.sogaz.site.paymentService.service.rabbit

import org.springframework.amqp.core.Message
import ru.sogaz.site.paymentService.dto.data.TaggedPayload

interface SendMessageProducer {
    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    )

    fun <T> toTaggedPayload(msg: Message, clazz: Class<T>): TaggedPayload<T>?
}
