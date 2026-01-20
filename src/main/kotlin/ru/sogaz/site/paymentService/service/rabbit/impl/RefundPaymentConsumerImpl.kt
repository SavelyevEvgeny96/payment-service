package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dto.data.RefundPayloadDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.PaidOrderMessageMapper
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RefundPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import ru.sogaz.site.paymentService.service.rabbit.impl.RecurringPaymentConsumerImpl.Companion.LOG_START

@Service
class RefundPaymentConsumerImpl(
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
) : RefundPaymentConsumer {
    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-refund-queue}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    ) {
        logger.info(LOG_START.format(messages.size))
        val started = System.nanoTime()
        // 1) Парсим сообщения → оставляем только валидные
        val payloads = messages.map { sendMessageProducer.toTaggedPayloadSafe(it, RefundPayloadDto::class.java) }
        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }
        logger.info(payloads.toString())
    }
}
