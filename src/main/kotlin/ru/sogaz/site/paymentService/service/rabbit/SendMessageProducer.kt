package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import ru.sogaz.site.paymentService.dto.data.ParsedResult

interface SendMessageProducer {
    fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    )

    fun <T : Any> parseMessage(
        messages: Message,
        channel: Channel,
        dtoClass: Class<T>,
    ): ParsedResult<T>?
}
