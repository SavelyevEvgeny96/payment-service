package ru.sogaz.site.paymentService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.paymentService.dto.data.ParsedResult
import ru.sogaz.site.paymentService.dto.data.PayloadInfoExtractor
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class SendMessageProducerImpl(
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate,
) : SendMessageProducer {
    companion object {
        val ORDER_ID_REGEX =
            Regex(
                """"orderId"\s*:\s*"([^"]+)"""",
                RegexOption.IGNORE_CASE,
            )

        val AUTHOR_REGEX =
            Regex(
                """"author"\s*:\s*"([^"]+)"""",
                RegexOption.IGNORE_CASE,
            )
    }

    private val logger = loggerFor(SendMessageProducerImpl::class.java)

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
                // Отключаем typeId
                message.messageProperties.headers.remove("__TypeId__")
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
     * Отправляет сообщение в RabbitMQ в «сыром» виде (без сериализации payload)
     * с использованием publisher confirms.
     *
     * <p>Метод предназначен для переотправки уже сформированного тела сообщения,
     * например:
     * <ul>
     *     <li>битого или неполного JSON</li>
     *     <li>сообщений, полученных из RabbitMQ и пересылаемых дальше</li>
     *     <li>форензики, технических очередей, внешней обработки</li>
     * </ul>
     *
     * <p><b>Особенности:</b>
     * <ul>
     *     <li>Сообщение отправляется как {@code byte[]} без участия
     *         {@link org.springframework.amqp.support.converter.MessageConverter}.</li>
     *     <li>Payload сохраняется в исходном виде (без экранирования и double-encoding).</li>
     *     <li>Используется синхронное ожидание publisher confirm от брокера.</li>
     * </ul>
     *
     * <p><b>Алгоритм:</b>
     * <ol>
     *     <li>Формируются базовые {@link AMQP.BasicProperties}
     *         (contentType, deliveryMode).</li>
     *     <li>Включается режим publisher confirms для канала.</li>
     *     <li>Сообщение публикуется через {@link Channel#basicPublish}.</li>
     *     <li>Метод блокируется до получения подтверждения от брокера.</li>
     *     <li>При отсутствии подтверждения выбрасывается исключение.</li>
     * </ol>
     *
     * <p><b>Гарантии:</b><br>
     * Метод либо завершается успешно (сообщение подтверждено брокером),
     * либо выбрасывает исключение, позволяя вызывающему коду
     * не выполнять ACK исходного сообщения.
     *
     * @param channel    RabbitMQ channel, используемый для publish и confirms
     * @param exchange   exchange, в который публикуется сообщение
     * @param routingKey routing key для маршрутизации сообщения
     * @param rawBody    тело сообщения в виде строки (отправляется без сериализации)
     *
     * @throws RuntimeException если брокер не подтвердил публикацию
     */
    override fun sendRawMessageWithConfirm(
        channel: Channel,
        exchange: String,
        routingKey: String,
        rawBody: String,
    ) {
        // Минимальный набор AMQP properties
        val props =
            AMQP.BasicProperties
                .Builder()
                .contentType("application/json")
                .deliveryMode(2) // persistent message
                .build()

        // Включаем publisher confirms для канала
        channel.confirmSelect()

        // Публикуем raw payload без сериализации
        channel.basicPublish(
            exchange,
            routingKey,
            props,
            rawBody.toByteArray(Charsets.UTF_8),
        )

        // Синхронно ожидаем подтверждения от брокера
        if (!channel.waitForConfirms(3000)) {
            throw RuntimeException("Сообщение не подтверждено брокером")
        }
    }

    override fun <T : Any> parseMessage(
        messages: Message,
        channel: Channel,
        dtoClass: Class<T>,
        payloadInfoExtractor: PayloadInfoExtractor,
    ): ParsedResult<T>? {
        val tag = messages.messageProperties.deliveryTag
        val messageId = messages.messageProperties.messageId
        val body = String(messages.body, Charsets.UTF_8)

        return try {
            // 1. Пробуем десериализовать тело
            val dto = objectMapper.readValue(body, dtoClass)
            ParsedResult.Success(tag, dto, messageId)
        } catch (ex: Exception) {
            // 2. Десериализация не удалась — пробуем вытащить полезную инфу
            val payloadInfo = payloadInfoExtractor.extract(body)

            if (payloadInfo != null) {
                // Сообщение битое, но мы знаем ЧТО-ТО важное → передаем дальше
                ParsedResult.Error(tag, body, payloadInfo, messageId, ex)
            } else {
                // Ничего полезного не нашли → реджект
                try {
                    channel.basicReject(tag, false)
                } catch (ackEx: Exception) {
                    logger.error("Не удалось сделать basicReject для tag=$tag", ackEx)
                }
                null
            }
        }
    }

    override fun extractAuthorUnsafe(body: String): String? {
        // 1. Пытаемся по-человечески
        runCatching {
            val node = objectMapper.readTree(body)
            val json =
                if (node.isTextual) objectMapper.readTree(node.asText()) else node

            return json
                .path("metaInfo")
                .firstOrNull()
                ?.path("author")
                ?.asText()
        }

        // 2. Fallback — режем строку
        return AUTHOR_REGEX
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
    }

    override fun extractOrderIdUnsafe(body: String): String? {
        // 1. Пытаемся по-человечески
        runCatching {
            val node = objectMapper.readTree(body)
            val json =
                if (node.isTextual) objectMapper.readTree(node.asText()) else node

            return json
                .path("orderId")
                .takeIf { !it.isMissingNode && !it.isNull }
                ?.asText()
        }

        // 2. Fallback — режем строку
        return ORDER_ID_REGEX
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
    }
}
