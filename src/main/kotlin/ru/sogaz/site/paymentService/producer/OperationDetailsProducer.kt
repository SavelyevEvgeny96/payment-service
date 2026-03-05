package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.mapper.v2.event.CompletedOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties

@Component
class OperationDetailsProducer(
    rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProperties,
    private val completedOperationMapper: CompletedOperationMapper,
) : RabbitProducer<CompletedOperationEvent>(rabbitTemplate) {
    fun sendOperationDetails(
        operation: IdempotentOrderOperation,
        bankOperationDetails: BankOperationDetails,
    ) = bankOperationDetails
        .mapToCompletedOperationEvent(operation)
        .convertAndSend()

    private fun BankOperationDetails.mapToCompletedOperationEvent(operation: IdempotentOrderOperation): CompletedOperationEvent =
        completedOperationMapper.completedOperationEvent(operation, this)

    private fun CompletedOperationEvent.convertAndSend(): Unit =
        convertAndSend(rabbitProperties.exchangeCompletedPayment, makeRoutingKey(), this, orderId)

    private fun CompletedOperationEvent.makeRoutingKey(): String =
        "${rabbitProperties.routingKeyDetailsPaymentPrefix}.${operationType.name.lowercase()}.${status.lowercase()}"
}
