package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import ru.sogaz.site.paymentService.dto.data.ParsedResult
import ru.sogaz.site.paymentService.dto.data.PayloadInfoExtractor

interface SendMessageProducer {
    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    )

    fun extractOrderIdUnsafe(body: String): String?

    fun extractAuthorUnsafe(body: String): String?

    fun sendRawMessageWithConfirm(
        channel: Channel,
        exchange: String,
        routingKey: String,
        rawBody: String,
    )

    fun <T : Any> parseMessage(
        messages: Message,
        channel: Channel,
        dtoClass: Class<T>,
        payloadInfoExtractor: PayloadInfoExtractor,
    ): ParsedResult<T>?
}
