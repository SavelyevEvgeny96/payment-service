package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest
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

@Service
class ReversalPayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardReversalIntegration: GpbCardReversalIntegration,
    private val gpbSbpReversalIntegration: GpbSbpReversalIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
) : ReversalPayOperationService {
    companion object {
        private const val REFUND_INTERNAL_ERROR = "Платежная система недоступна"
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
            .refundCardPayOperationCommand()
            .runRefundCommand()

    private fun reversalSbpPayOperation(refundOperationRequest: ReversalOperationRequest): BankOperationDetails =
        refundOperationRequest
            .reversalSbpPayOperationCommand()
            .runRefundCommand()

    /**
     * Формирует объект команды и стратегию банковской операции по возврату оплаты картой относительно этого запроса.
     * Добавляет план действий при возбуждении ошибки и при финальном статусе операции.
     */
    private fun ReversalOperationRequest.refundCardPayOperationCommand() =
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
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    /**
     * Общая функция для запуска выполнения команды в сервисе операций
     */
    private fun <RESULT> OperationCommand<ReversalOperationRequest, RESULT>.runRefundCommand(): RESULT = operationService.runOperation(this)
}
