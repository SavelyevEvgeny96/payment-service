package ru.sogaz.site.paymentService.service.v2.operation.impl

import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.model.StepResult
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
@Transactional
class OperationServiceImpl(
    private val idempotentOrderService: IdempotentOrderService,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
) : OperationService {
    override fun <REQUEST : OperationRequest, RESULT> runIdempotentOperation(
        operationCommand: OperationCommand<REQUEST, RESULT>,
    ): Result<RESULT> =
        runCatching {
            operationCommand
                .run(::createNewIdempotentOrderOperation)
                .also(checkOperationStatusProducer::sendDelayedCheckStatusEvent)
                .executeCommand(operationCommand)
                .onFinalState(operationCommand)
                .result
        }

    private fun <REQUEST : OperationRequest, RESULT> IdempotentOrderOperation.executeCommand(
        operationCommand: OperationCommand<REQUEST, RESULT>,
    ): StepResult<RESULT> = operationCommand.strategy.execute(this, idempotentOrderService::saveOperation)

    private fun <REQUEST : OperationRequest, RESULT> createNewIdempotentOrderOperation(
        operationCommand: OperationCommand<REQUEST, RESULT>,
    ) = when {
        operationCommand.requestToOrderOperationMapper == null -> idempotentOrderService.saveOperation(operationCommand.request)
        else -> idempotentOrderService.saveOperation(operationCommand.request, operationCommand.requestToOrderOperationMapper)
    }

    private fun <REQUEST : OperationRequest, RESULT> StepResult<RESULT>.onFinalState(
        operationCommand: OperationCommand<REQUEST, RESULT>,
    ): StepResult<RESULT> = applyIf(operation.state.isFinaleState()) { apply { operationCommand.finalStateAction(operation, result) } }
}
