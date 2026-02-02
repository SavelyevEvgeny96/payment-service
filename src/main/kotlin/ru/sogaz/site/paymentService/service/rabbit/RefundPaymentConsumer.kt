package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message

interface RefundPaymentConsumer {
    fun handleBatch(
        messages:Message,
        channel: Channel,
    )
}
