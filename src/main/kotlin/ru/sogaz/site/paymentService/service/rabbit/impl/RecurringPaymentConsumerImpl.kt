package ru.sogaz.site.paymentService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.config.converters.NoOpMessageConverter
import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RecurringPaymentConsumer

@Service
class RecurringPaymentConsumerImpl(
    private val messageConverter: NoOpMessageConverter,
    private val objectMapper: ObjectMapper,
    private val buildBatchConsumerService: BuildBatchConsumerService,
) : RecurringPaymentConsumer {
    companion object {
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
    }

    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-create-queue}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    ) {
        val started = System.nanoTime()
        val payloads = mutableListOf<Pair<Long, PaymentCreatedEventDto>>() // tag + dto

        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            try {
                val dto = objectMapper.readValue(msg.body, PaymentCreatedEventDto::class.java)
                payloads += tag to dto
            } catch (ex: Exception) {
                logger.error("Ошибка парсинга сообщения: ${msg.messageProperties.messageId} (tag=$tag)", ex)
            }
        }

        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        try {
            val dtos = payloads.map { it.second }
            buildBatchConsumerService.upsertBatch(dtos)
            // ACK только за успешно обработанные
            payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }
            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)
        }
    }
}
