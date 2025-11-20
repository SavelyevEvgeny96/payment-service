package ru.sogaz.site.paymentService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.config.converters.NoOpMessageConverter
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.payment.PaymentStatusServiceImpl.Companion.START_LOG_MESSAGE_QUEUE
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RecurringPaymentConsumer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
@Service
class RecurringPaymentConsumerImpl(
    private val objectMapper: ObjectMapper,
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val rabbitTemplate: RabbitTemplate,
    private val props: RabbitProperties
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

        // 1) Парсим сообщения → оставляем только валидные
        val payloads: List<TaggedPayload> = messages
            .mapNotNull(::toTaggedPayload)

        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        // 2) Обрабатываем батч в сервисе
        val batchResult = runCatching {
            val dtos = payloads.map { it.dto }
            buildBatchConsumerService.upsertBatch(dtos)
        }.onFailure { ex ->
            logger.error("Ошибка при обработке батча: ${ex.message}", ex)
        }.getOrNull() ?: return

        // 3) Отправляем статусы paid/unpaid
        sendMessagePaid(batchResult.paid)
        sendMessageUnpaid(batchResult.unpaid)

        // 4) ACK за все успешно распарсенные сообщения
        payloads.forEach { payload ->
            channel.basicAck(payload.tag, false)
        }

        val tookMs = (System.nanoTime() - started) / 1_000_000
        logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
    }

    /**
     * Преобразует Message → TaggedPayload или логирует ошибку и возвращает null
     */
    private fun toTaggedPayload(msg: Message): TaggedPayload? =
        runCatching {
            val tag = msg.messageProperties.deliveryTag
            val dto = objectMapper.readValue(msg.body, OrderPayloadDto::class.java)
            TaggedPayload(tag, dto)
        }.onFailure { ex ->
            val props = msg.messageProperties
            logger.error(
                "Ошибка парсинга сообщения: ${props.messageId} (tag=${props.deliveryTag})",
                ex
            )
        }.getOrNull()

    // --- Отправка сообщений о статусе ---

    private fun sendMessagePaid(orderIds: List<UUID?>) {
        sendOrderStatus(props.routingKeyStatusOrderPaid, orderIds)
    }

    private fun sendMessageUnpaid(orderIds: List<UUID?>) {
        sendOrderStatus(props.routingKeyStatusOrderUnpaid, orderIds)
    }

    private fun sendOrderStatus(
        routingKey: String,
        orderIds: List<UUID?>,
    ) {
        if (orderIds.isEmpty()) {
            logger.info("Нет orderId для отправки в очередь routingKey=$routingKey")
            return
        }
        val exchange = props.exchangeOrder
        val timestamp = OffsetDateTime
            .now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        logger.info(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            orderIds,
        ) { message ->
            message.messageProperties.headers["author"] = "payService"
            message.messageProperties.headers["flowCode"] = "ResultPay"
            message.messageProperties.headers["timestamp"] = timestamp
            message
        }
    }
}