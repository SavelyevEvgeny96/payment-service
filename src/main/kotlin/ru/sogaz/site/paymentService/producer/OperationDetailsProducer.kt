package ru.sogaz.site.paymentService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.mapper.v2.event.CompletedOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.event.CompletedOperationEvent

/**
 * Компонент для отправки деталей завершенных операции (статусов) через RabbitMQ.
 */
@Component
class OperationDetailsProducer(
    rabbitTemplate: RabbitTemplate,
    @Value("\${app.rabbit.exchange-completed-payment}")
    exchange: String,
    @param:Value("\${app.rabbit.routing-key-details-payment-prefix}")
    private val baseRoutingKey: String,
    private val completedOperationMapper: CompletedOperationMapper,
) : RabbitProducer<CompletedOperationEvent>(rabbitTemplate, exchange) {
    /**
     * Отправляет детали успешной операции с динамическим ключом маршрутизации.
     *
     * @param operation операция для отправки
     * @param bankOperationDetails детали банковской операции
     */
    fun sendOperationDetails(
        operation: IdempotentOrderOperation,
        bankOperationDetails: BankOperationDetails,
    ) = operation
        .mapToCompletedOperationEvent(bankOperationDetails)
        .convertAndSend()

    /**
     * Отправляет детали неудачной операции с динамическим ключом маршрутизации.
     *
     * @param operation операция для отправки
     * @param errorText текст ошибки (по умолчанию "Внутренняя ошибка сервиса")
     */
    fun sendFailureOperationDetails(
        operation: IdempotentOrderOperation,
        errorText: String = "Внутренняя ошибка сервиса",
    ) = sendOperationDetails(
        operation,
        BankOperationDetails(operation.paymentBankId ?: "", OperationState.FAIL, errorText = errorText),
    )

    /**
     * Преобразует операцию в сообщение с деталями завершенной операции.
     *
     * @param details детали банковской операции
     * @return событие успешной операции
     */
    private fun IdempotentOrderOperation.mapToCompletedOperationEvent(details: BankOperationDetails): CompletedOperationEvent =
        completedOperationMapper.completedOperationEvent(this, details)

    /**
     * На основе объекта события формирует ключ и проставляет correlationId в отправляемом запросе.
     */
    private fun CompletedOperationEvent.convertAndSend(): Unit = convertAndSend(makeRoutingKey(), this, orderId)

    /**
     * Генерирует ключ маршрутизации для события на основе типа операции и финального статуса.
     * Данный динамический ключ используется для гибкой маршрутизации событий по очередям.
     *
     * @return ключ маршрутизации
     */
    private fun CompletedOperationEvent.makeRoutingKey(): String = "$baseRoutingKey.${operationType.name.lowercase()}.${status.lowercase()}"
}
