package ru.sogaz.site.paymentService.service.rabbit

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message

interface RecurringPaymentConsumer {
    fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    )
}