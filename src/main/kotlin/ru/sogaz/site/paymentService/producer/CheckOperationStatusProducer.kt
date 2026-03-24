package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CheckStatusEvent

/**
 * RabbitProducer для отправки событий о необходимости проверки статуса банковской операции.
 * Использует базовую реализацию взаимодействия с rabbitTemplate.
 * Логика распределения событий по очередям управляется в rabbit указанием routing key в соответствующем exchange
 * В случае, если для отправленного ключа нет прописанного маршрута, то он отправляется в alternate exchange
 */
@Component
class CheckOperationStatusProducer(
    rabbitTemplate: RabbitTemplate,
    @Value("\${app.rabbit.exchange-check-status-payment}")
    exchange: String,
    @param:Value("\${app.rabbit.routing-key-check-status-payment}")
    private val baseRoutingKey: String,
) : RabbitProducer<CheckStatusEvent>(rabbitTemplate, exchange) {
    /**
     * Отправляет событие проверки статуса на основе операции.
     *
     * @param operation операция для которой нужно проверить статус
     */
    fun sendCheckStatusEvent(operation: IdempotentOrderOperation) =
        convertAndSend(
            routingKey = baseRoutingKey,
            message = CheckStatusEvent(operation.id!!),
            correlationId = operation.idempotentOrder?.id,
            messagePostProcessor = setMessageDeathCount(0),
        )

    /**
     * Отправляет событие проверки статуса с задержкой.
     *
     * @param event событие для отправки c id операции
     * @param deathCount количество повторных попыток до текущей
     */
    fun sendDelayedCheckStatusEvent(
        event: CheckStatusEvent,
        deathCount: Int = 0,
    ) = convertAndSend(
        routingKey = makeRoutingKey(deathCount),
        message = CheckStatusEvent(event.operationId),
        messagePostProcessor = setMessageDeathCount(deathCount),
    )

    /**
     * Отправляет событие проверки статуса с задержкой на основе операции.
     *
     * @param operation операция для которой нужно проверить статус
     * @param deathCount количество повторных попыток до текущей
     */
    fun sendDelayedCheckStatusEvent(
        operation: IdempotentOrderOperation,
        deathCount: Int = 0,
    ) = convertAndSend(
        routingKey = makeRoutingKey(deathCount),
        message = CheckStatusEvent(operation.id!!),
        correlationId = operation.idempotentOrder?.id,
        messagePostProcessor = setMessageDeathCount(deathCount),
    )

    /**
     * Формирует ключ маршрутизации на основе количества попыток.
     *
     * @param deathCount количество повторных попыток
     * @return сформированный ключ маршрутизации
     */
    private fun makeRoutingKey(deathCount: Int) = "$baseRoutingKey.$deathCount"

    /**
     * Устанавливает заголовок для количества попыток доставки сообщения.
     *
     * @param deathCount количество повторных попыток
     * @return функция-обработчик, добавляющая заголовок в сообщение
     */
    private fun setMessageDeathCount(deathCount: Int): (Message) -> Message =
        { it.apply { messageProperties.headers["x-death-count"] = deathCount } }
}
