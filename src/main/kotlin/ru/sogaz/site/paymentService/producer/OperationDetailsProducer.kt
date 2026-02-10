package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.mapper.v2.event.CompletedOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties

@Component
class OperationDetailsProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProperties,
    private val completedOperationMapper: CompletedOperationMapper,
) {
    fun sendOperationDetails(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ) {
        val completedOperationEvent = completedOperationMapper.completedOperationEvent(operation, operationDetails)
        rabbitTemplate.convertAndSend(
            rabbitProperties.exchangePayment,
            makeRoutingKey(completedOperationEvent),
            completedOperationEvent,
            CorrelationData(operation.idempotentOrder.orderId.toString()),
        )
    }

    private fun makeRoutingKey(completedOperationEvent: CompletedOperationEvent): String =
        "${rabbitProperties.routingKeyDetailsPaymentPrefix}.${completedOperationEvent.status.lowercase()}"
}
