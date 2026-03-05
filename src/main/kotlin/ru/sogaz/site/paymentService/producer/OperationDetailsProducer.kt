package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.mapper.v2.event.CompletedOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent

@Component
class OperationDetailsProducer(
    rabbitTemplate: RabbitTemplate,
    @Value("\${app.rabbit.exchange-completed-payment}")
    exchange: String,
    @param:Value("\${app.rabbit.routing-key-details-payment-prefix}")
    private val baseRoutingKey: String,
    private val completedOperationMapper: CompletedOperationMapper,
) : RabbitProducer<CompletedOperationEvent>(rabbitTemplate, exchange) {
    fun sendOperationDetails(
        operation: IdempotentOrderOperation,
        bankOperationDetails: BankOperationDetails,
    ) = bankOperationDetails
        .mapToCompletedOperationEvent(operation)
        .convertAndSend()

    private fun BankOperationDetails.mapToCompletedOperationEvent(operation: IdempotentOrderOperation): CompletedOperationEvent =
        completedOperationMapper.completedOperationEvent(operation, this)

    private fun CompletedOperationEvent.convertAndSend(): Unit =
        convertAndSend(makeRoutingKey(), this, orderId)

    private fun CompletedOperationEvent.makeRoutingKey(): String =
        "$baseRoutingKey.${operationType.name.lowercase()}.${status.lowercase()}"
}
