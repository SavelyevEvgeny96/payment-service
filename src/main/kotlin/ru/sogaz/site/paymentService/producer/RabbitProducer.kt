package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import java.util.UUID

/**
 * Базовый класс для отправки сообщений в RabbitMQ.
 *
 * @param <T> тип сообщения, которое будет отправляться
 */
abstract class RabbitProducer<T>(
    private val rabbitTemplate: RabbitTemplate,
    private val exchange: String,
) {
    protected fun convertAndSend(
        routingKey: String,
        message: T,
        correlationId: UUID? = null,
    ): Unit =
        convertAndSend(
            routingKey,
            message!!,
            correlationId,
            null,
        )

    protected fun convertAndSend(
        routingKey: String,
        message: T,
        messagePostProcessor: MessagePostProcessor,
    ): Unit =
        convertAndSend(
            routingKey,
            message!!,
            null,
            messagePostProcessor,
        )

    /**
     * Отправляет сообщение с указанным ключом маршрутизации, correlationId и
     * базовой реализацией постпроцессора,
     * которая добавляет в хедеры каждого сообщения Exchange и RoutingKey и удаляет __TypeId__, а также
     * запускает постпроцессор из параметров.
     *
     * @param routingKey ключ маршрутизации
     * @param message сообщение для отправки
     * @param correlationId корреляционный идентификатор (опционально)
     * @param messagePostProcessor процессор сообщений (опционально)
     */
    protected fun convertAndSend(
        routingKey: String,
        message: T,
        correlationId: UUID?,
        messagePostProcessor: MessagePostProcessor?,
    ): Unit =
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            message!!,
            {
                it.apply {
                    messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                    messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                    messageProperties.headers.remove("__TypeId__")
                    messagePostProcessor?.postProcessMessage(this)
                }
            },
            correlationId?.let { CorrelationData(it.toString()) },
        )
}
