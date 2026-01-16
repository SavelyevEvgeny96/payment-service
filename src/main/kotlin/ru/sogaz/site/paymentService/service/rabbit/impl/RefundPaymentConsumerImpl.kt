package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.PaidOrderMessageMapper
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RefundPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer

class RefundPaymentConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paidOrderMessageMapper: PaidOrderMessageMapper,
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
        val started = System.nanoTime()
        // 1) Парсим сообщения → оставляем только валидные
        val payloads = messages.mapNotNull { sendMessageProducer.toTaggedPayload(it, OrderPayloadDto::class.java) }
        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }
    }
}