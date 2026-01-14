package ru.sogaz.site.paymentService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SendMessageProducerImpl(
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate,
) : SendMessageProducer {
    private val logger = loggerFor(BuildBatchConsumerServiceImpl::class.java)

    /**
     * Универсальный метод отправки сообщения в RabbitMQ.
     *
     * Метод generic, чтобы не использовать `Any` и сохранить тип payload на уровне Kotlin.
     * Фактическая сериализация всё равно выполняется Spring AMQP `MessageConverter` (обычно Jackson).
     *
     * Что делает метод:
     * 1) Генерирует timestamp для headers.
     * 2) Создаёт `CorrelationData` для трассировки / publisher confirms.
     * 3) Отправляет сообщение в exchange + routingKey.
     * 4) Дополняет message headers технической информацией.
     *
     * Важные поля:
     * - `messageProperties.correlationId` — correlation id внутри message properties
     * - `CorrelationData`               — отдельная структура Spring для корреляции confirm'ов
     *
     * @param routingKey routing key, по которому маршрутизируем сообщение
     * @param payload объект полезной нагрузки (DTO)
     * @param exchange exchange, в который отправляем сообщение
     * @param orderId бизнес-корреляция (если null — создаём случайный correlation id)
     */
    override fun <T : Any> sendMessage(
        routingKey: String,
        payload: T,
        exchange: String,
        orderId: String?,
    ) {
        // --- 1) Формируем timestamp в UTC для заголовков ---
        val timestamp =
            OffsetDateTime
                .now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        // --- 2) Формируем correlationId для confirm/trace ---
        val correlationId = orderId ?: ""

        // CorrelationData — объект Spring AMQP для publisher confirms и корреляции.
        val cd = CorrelationData(correlationId)

        // --- 3) Публикация сообщения ---
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            payload,
            // --- 4) MessagePostProcessor: дополняем заголовки/свойства перед отправкой ---
            { message ->

                // 4.1) Технические headers для трассировки и диагностики
                message.messageProperties.headers["author"] = "payService"
                message.messageProperties.headers["flowCode"] = "ResultPay"
                message.messageProperties.headers["timestamp"] = timestamp

                // 4.2) Пробрасываем exchange/routingKey в headers (удобно для логов/трассировки)
                message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey

                // 4.3) CorrelationId внутри message properties (может использоваться consumer’ом)
                message.messageProperties.correlationId = orderId

                message
            },
            cd,
        )
    }
    /**
     * Преобразует Message → TaggedPayload или логирует ошибку и возвращает null
     */
    override fun <T> toTaggedPayload(msg: Message, clazz: Class<T>): TaggedPayload<T>? =
        runCatching {
            val tag = msg.messageProperties.deliveryTag
            val dto = objectMapper.readValue(msg.body, clazz)
            TaggedPayload(tag, dto)
        }.onFailure { ex ->
            val props = msg.messageProperties
            logger.error(
                "Ошибка парсинга сообщения: ${props.messageId} (tag=${props.deliveryTag})",
                ex,
            )
        }.getOrNull()
}
