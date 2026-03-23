package ru.sogaz.site.paymentService.producer

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CheckStatusEvent

@Component
class CheckOperationStatusProducer(
    rabbitTemplate: RabbitTemplate,
    @Value("\${app.rabbit.exchange-check-status-payment}")
    exchange: String,
    @param:Value("\${app.rabbit.routing-key-check-status-payment}")
    private val baseRoutingKey: String,
    private val maxDeathCount: Int = 5,
) : RabbitProducer<CheckStatusEvent>(rabbitTemplate, exchange) {
    fun sendCheckStatusEvent(operation: IdempotentOrderOperation) =
        convertAndSend(
            baseRoutingKey,
            CheckStatusEvent(operation.id!!),
            operation.idempotentOrder.id,
            setMessageDeathCount(0),
        )

    fun sendDelayedCheckStatusEvent(
        event: CheckStatusEvent,
        deathCount: Int = 0,
    ) = convertAndSend(
        makeRoutingKey(deathCount),
        CheckStatusEvent(event.operationId),
        setMessageDeathCount(deathCount),
    )

    fun sendDelayedCheckStatusEvent(
        operation: IdempotentOrderOperation,
        deathCount: Int = 0,
    ) = convertAndSend(
        makeRoutingKey(deathCount),
        CheckStatusEvent(operation.id!!),
        operation.idempotentOrder.id,
        setMessageDeathCount(deathCount),
    )

    private fun makeRoutingKey(deathCount: Int) = "$baseRoutingKey.${deathCount.butIf(deathCount > maxDeathCount) { maxDeathCount }}"

    private fun setMessageDeathCount(deathCount: Int): (Message) -> Message =
        { message ->
            message.messageProperties.headers["x-death-count"] = deathCount
            message
        }
}
