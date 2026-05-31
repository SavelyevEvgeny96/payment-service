package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.reversal.ReversalOperationRequest
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardReversalIntegration
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpReversalIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.gpbOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFailure
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFinalState
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.ReversalPayOperationService
import ru.sogaz.site.paymentService.service.v2.rules.RulePaymentTypeService

@Service
class ReversalPayOperationServiceImpl(
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
    private val operationService: OperationService,
    private val gpbCardReversalIntegration: GpbCardReversalIntegration,
    private val gpbSbpReversalIntegration: GpbSbpReversalIntegration,
    private val rulePaymentTypeService: RulePaymentTypeService,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
) : ReversalPayOperationService {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val REFUND_INTERNAL_ERROR = "Платежная система недоступна"
        private const val OPERATION_NOT_AVAILABLE_ERROR = "Операция недоступна для выбранного способа оплаты"
    }

    /**
     * Формирует команду и стратегию для выполнения возврата платежа в банке.
     *
     * @param reversalOperationRequest запрос на выполнение операции оплаты
     * @return детали выполненного рекуррентного платежа
     */
    override fun reversalPayOperation(reversalOperationRequest: ReversalOperationRequest): BankOperationDetails =
        when (reversalOperationRequest.paymentType) {
            PaymentType.CARD -> reversalCardPayOperation(reversalOperationRequest)
            PaymentType.SBP -> reversalSbpPayOperation(reversalOperationRequest)
        }

    /**
     * Формирует команду и стратегию для выполнения возврата платежа картой в банке.
     *
     * @param reversalOperationRequest запрос на выполнение операции оплаты
     * @return детали выполненного рекуррентного платежа
     */
    private fun reversalCardPayOperation(reversalOperationRequest: ReversalOperationRequest): BankOperationDetails =
        reversalOperationRequest
            .checkAvailability()
            .reversalCardPayOperationCommand()
            .runReversalCommand()

    private fun reversalSbpPayOperation(reversalOperationRequest: ReversalOperationRequest): BankOperationDetails =
        reversalOperationRequest
            .checkAvailability()
            .reversalSbpPayOperationCommand()
            .runReversalCommand()

    /**
     * Формирует объект команды и стратегию банковской операции по возврату оплаты картой относительно этого запроса.
     * Добавляет план действий при возбуждении ошибки и при финальном статусе операции.
     */
    private fun ReversalOperationRequest.reversalCardPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            stepWithSave(
                action = gpbCardReversalIntegration::reversalPayCard,
                resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
            ),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, REFUND_INTERNAL_ERROR)
        } onFinalState {
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    private fun ReversalOperationRequest.reversalSbpPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            stepWithSave(
                action = gpbSbpReversalIntegration::reversalPaySbp,
                resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
            ),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, REFUND_INTERNAL_ERROR)
        } onFinalState {
            checkOperationStatusProducer.sendCheckStatusEvent(this)
        }

    /**
     * Проверяет доступность операции по правилу.
     */
    private fun ReversalOperationRequest.checkAvailability(): ReversalOperationRequest {
        val available = rulePaymentTypeService.isOperationAvailable(operationType, paymentType, bank)
        if (available) return this

        logger.warn(
            "Операция отмены недоступна по правилу. operationType [{}], paymentType [{}], bank [{}], orderId [{}]",
            operationType,
            paymentType,
            bank,
            orderId,
        )
        throw InnerException(getTraceId(), OPERATION_NOT_AVAILABLE_ERROR)
    }

    /**
     * Общая функция для запуска выполнения команды в сервисе операций
     */
    private fun <RESULT> OperationCommand<ReversalOperationRequest, RESULT>.runReversalCommand(): RESULT =
        operationService.runOperation(this)
}
