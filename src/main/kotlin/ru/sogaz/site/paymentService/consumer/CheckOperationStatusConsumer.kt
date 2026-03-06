package ru.sogaz.site.paymentService.consumer

import io.github.resilience4j.retry.annotation.Retry
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.event.CheckStatusEvent
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.status.OperationDetailsService
import java.time.Instant

@Component
class CheckOperationStatusConsumer(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val operationDetailsService: OperationDetailsService,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
    private val operationDetailsProducer: OperationDetailsProducer,
) {
    @RabbitListener(
        queues = ["\${app.rabbit.payment-status-check-queue}"],
        containerFactory = "concurrentContainerFactory",
    )
    @Retry(name = "rabbitFastRetry", fallbackMethod = "requeue")
    fun checkStatus(
        @Payload checkStatusEvent: CheckStatusEvent,
        @Header("x-death-count") deathCount: Int?,
    ) {
        val operation = idempotentOrderOperationDao.findById(checkStatusEvent.operationId)
        if (operation == null || operation.state.isFinaleState()) {
            return
        }
        val operationDetails = operationDetailsService.getOperationDetails(operation)
        when (operationDetails.state) {
            OperationState.SUCCESS,
            OperationState.FAIL,
            OperationState.REFUND,
            OperationState.DECLINED,
            -> handleCompletedOperation(operation, operationDetails)
            else
            -> checkOperationStatusProducer.sendDelayedCheckStatusEvent(operation, increaseDeathCount(deathCount))
        }
    }

    private fun handleCompletedOperation(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ) {
        operationDetailsProducer.sendOperationDetails(operation, operationDetails)
        operation.state = operationDetails.state
        operation.operationFinished = Instant.now()
        idempotentOrderOperationDao.saveAndFlush(operation)
    }

    private fun increaseDeathCount(deathCount: Int?): Int = (deathCount ?: -1) + 1

    fun requeue(
        event: CheckStatusEvent,
        deathCount: Int?,
        ex: Exception,
    ): Unit = checkOperationStatusProducer.sendDelayedCheckStatusEvent(event, increaseDeathCount(deathCount))
}
