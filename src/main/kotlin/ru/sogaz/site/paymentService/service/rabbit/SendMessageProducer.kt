package ru.sogaz.site.paymentService.service.rabbit

import org.springframework.amqp.core.Message
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.dto.response.ParseResult

interface SendMessageProducer {
    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    )

    fun <T> parsePayload(
        msg: Message,
        clazz: Class<T>,
    ): TaggedPayload<T>

    fun <T> toTaggedPayloadSafe(
        msg: Message,
        clazz: Class<T>,
    ): ParseResult<T>
}
