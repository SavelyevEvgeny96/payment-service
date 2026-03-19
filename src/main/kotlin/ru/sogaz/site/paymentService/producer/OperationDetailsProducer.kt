package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.mapper.v2.event.CompletedOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
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
    ) = operation
        .mapToCompletedOperationEvent(bankOperationDetails)
        .convertAndSend()

    fun sendFailureOperationDetails(
        operation: IdempotentOrderOperation,
        errorText: String = "Внутренняя ошибка сервиса"
    ) = sendOperationDetails(
        operation,
        BankOperationDetails(operation.paymentBankId ?: "", OperationState.FAIL, errorText = errorText)
    )

    fun sendOperationDetails(operation: IdempotentOrderOperation) =
        operation
            .mapToCompletedOperationEvent()
            .convertAndSend()

    private fun IdempotentOrderOperation.mapToCompletedOperationEvent(details: BankOperationDetails): CompletedOperationEvent =
        completedOperationMapper.completedOperationEvent(this, details)

    private fun IdempotentOrderOperation.mapToCompletedOperationEvent(): CompletedOperationEvent =
        completedOperationMapper.completedOperationEvent(this)

    private fun CompletedOperationEvent.convertAndSend(): Unit = convertAndSend(makeRoutingKey(), this, orderId)

    private fun CompletedOperationEvent.makeRoutingKey(): String = "$baseRoutingKey.${operationType.name.lowercase()}.${status.lowercase()}"
}
