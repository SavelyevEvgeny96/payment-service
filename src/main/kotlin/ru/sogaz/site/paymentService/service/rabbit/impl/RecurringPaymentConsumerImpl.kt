package ru.sogaz.site.paymentService.service.rabbit.impl
import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import ru.sogaz.site.paymentService.loggerFor
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import ru.sogaz.site.paymentService.config.converters.NoOpMessageConverter
import ru.sogaz.site.paymentService.service.rabbit.RecurringPaymentConsumer

class RecurringPaymentConsumerImpl(
    private val messageConverter: NoOpMessageConverter,
    private val objectMapper: ObjectMapper
) : RecurringPaymentConsumer {
    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)
    @RabbitListener(
        queues = ["\${app.rabbit.queue-order}"],
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
                // ничего не делаем — не ack'аем, чтобы сообщение осталось в очереди
            }
        }

        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        try {
            val dtos = payloads.map { it.second }
            processBatch(dtos)

            // ACK только за успешно обработанные
            payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }

            val tookMs = (System.nanoTime() - started) / 1_000_000
//        logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)
            // тоже ничего не ACK'аем — чтобы RabbitMQ сам повторил
        }
    }

    private fun processBatch(events: List<PaymentCreatedEventDto>) {
        // твоя бизнес-логика, без каналов и сообщений
    }


}
