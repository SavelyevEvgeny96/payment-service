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

        // парсим сообщения
        messages.forEach { msg ->
            val tag = msg.messageProperties.deliveryTag
            try {
                // 1) логируем headers для отладки
                val headers = msg.messageProperties.headers ?: emptyMap<String, Any?>()
                logger.debug("Входящие заголовки для tag=$tag: $headers")

                // 2) получаем результат из messageConverter — он может вернуть DTO или Map
                val raw =
                    try {
                        messageConverter.fromMessage(msg)
                    } catch (ex: Exception) {
                        logger.warn("messageConverter.fromMessage ошибочно для tag=$tag, вернемся к разбору тела", ex)
                        null
                    }
                // 3) пытаемся привести/сконвертировать в OrderPayloadDto разными способами
                val dto =
                    when (raw) {
                        is PaymentCreatedEventDto -> raw
                        is Map<*, *> -> objectMapper.convertValue(raw, PaymentCreatedEventDto::class.java)
                        is String -> objectMapper.readValue(raw, PaymentCreatedEventDto::class.java)
                        null -> {
                            // если converter упал или вернул null — парсим body напрямую
                            val body = String(msg.body, Charsets.UTF_8)
                            objectMapper.readValue(body, PaymentCreatedEventDto::class.java)
                        }

                        else -> {
                            // на случай непредвиденных типов — пробуем через body
                            val body = String(msg.body, Charsets.UTF_8)
                            objectMapper.readValue(body, PaymentCreatedEventDto::class.java)
                        }
                    }

                payloads += tag to dto
            } catch (ex: Exception) {
                logger.error("Ошибка парсинга сообщения: ${msg.messageProperties.messageId} (tag=$tag)", ex)
                // отправляем битое сообщение в DLQ
                channel.basicReject(tag, false)
            }
        }

        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        // далее — без изменений (upsert, publish, ack/ reject)
        try {


            payloads.forEach { (tag, _) -> channel.basicAck(tag, false) }

            val tookMs = (System.nanoTime() - started) / 1_000_000
            logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
        } catch (ex: Exception) {
            logger.error("Ошибка при обработке валидных сообщений батча: ${ex.message}", ex)
            payloads.forEach { (tag, _) -> channel.basicReject(tag, false) }
        }
    }


}
}