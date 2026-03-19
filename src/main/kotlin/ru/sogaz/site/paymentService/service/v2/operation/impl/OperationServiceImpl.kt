package ru.sogaz.site.paymentService.service.v2.operation.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
@Transactional
class OperationServiceImpl(
    private val idempotentOrderService: IdempotentOrderService,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
) : OperationService {
    override fun <REQUEST : OperationRequest, RESULT> runOperation(
        operationCommand: OperationCommand<REQUEST, RESULT>
    ): Result<RESULT> =
        operationCommand
            .createNewIdempotentOrderOperation()
            .also(checkOperationStatusProducer::sendDelayedCheckStatusEvent)
            .executeCatching(operationCommand)

    private fun <REQUEST : OperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.createNewIdempotentOrderOperation() =
        idempotentOrderService.saveOperation(request, bank)

    private fun <REQUEST : OperationRequest, RESULT> IdempotentOrderOperation.executeCatching(
        operationCommand: OperationCommand<REQUEST, RESULT>,
    ): Result<RESULT> =
        runCatching {
            operationCommand.strategy.execute(this, idempotentOrderService::saveOperation).result
        }.onSuccess {
            if (state.isFinaleState()) {
                operationCommand.finalStateAction(this, it)
            }
        }.onFailure {
            operationCommand.onFailureAction(this, it)
        }
}
