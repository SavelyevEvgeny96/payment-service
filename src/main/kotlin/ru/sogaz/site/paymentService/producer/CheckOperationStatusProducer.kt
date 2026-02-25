package ru.sogaz.site.paymentService.producer

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CheckStatusEvent
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties

@Component
class CheckOperationStatusProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProperties,
    private val maxDeathCount: Int = 5,
) {
    fun sendDelayedCheckStatusEvent(
        event: CheckStatusEvent,
        deathCount: Int = 0,
    ) {
        rabbitTemplate.convertAndSend(
            rabbitProperties.exchangeCheckStatusPayment,
            makeRoutingKey(deathCount),
            CheckStatusEvent(event.operationId),
            setMessageDeathCount(deathCount),
        )
    }

    fun sendDelayedCheckStatusEvent(
        operation: IdempotentOrderOperation,
        deathCount: Int = 0,
    ) {
        rabbitTemplate.convertAndSend(
            rabbitProperties.exchangeCheckStatusPayment,
            makeRoutingKey(deathCount),
            CheckStatusEvent(operation.id!!),
            setMessageDeathCount(deathCount),
            CorrelationData(operation.idempotentOrder?.id.toString()),
        )
    }

    private fun makeRoutingKey(deathCount: Int) =
        "${rabbitProperties.routingKeyCheckStatusPayment}.${deathCount.butIf(deathCount > maxDeathCount) { maxDeathCount }}"

    private fun setMessageDeathCount(deathCount: Int): (Message) -> Message =
        { message ->
            message.messageProperties.headers["x-death-count"] = deathCount
            message
        }
}
