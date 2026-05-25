package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardRefundIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.gpbOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFailure
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFinalState
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.RefundPayOperationService
import ru.sogaz.site.paymentService.service.v2.rules.RulePaymentTypeService

@Service
class RefundPayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardRefundIntegration: GpbCardRefundIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
    private val rulePaymentTypeService: RulePaymentTypeService,
) : RefundPayOperationService {
    private val logger = loggerFor(javaClass)
    companion object {
        private const val REFUND_TYPE_ERROR = "Не поддерживаемый для отмены тип платежа"
        private const val REFUND_INTERNAL_ERROR = "Платежная система недоступна"
        private const val OPERATION_NOT_AVAILABLE_ERROR = "Операция недоступна для выбранного способа оплаты"
    }

    /**
     * Формирует команду и стратегию для выполнения возврата платежа в банке.
     *
     * @param refundOperationRequest запрос на выполнение операции оплаты
     * @return детали выполненного рекуррентного платежа
     */
    override fun refundPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails =
        when (refundOperationRequest.paymentType) {
            PaymentType.CARD -> refundCardPayOperation(refundOperationRequest)
            else -> throw InnerException(getTraceId(), REFUND_TYPE_ERROR)
        }

    /**
     * Формирует команду и стратегию для выполнения возврата платежа картой в банке.
     *
     * @param refundOperationRequest запрос на выполнение операции оплаты
     * @return детали выполненного рекуррентного платежа
     */
    override fun refundCardPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails =
        refundOperationRequest
            .checkAvailability()
            .refundCardPayOperationCommand()
            .runRefundCommand()

    /**
     * Формирует объект команды и стратегию банковской операции по возврату оплаты картой относительно этого запроса.
     * Добавляет план действий при возбуждении ошибки и при финальном статусе операции.
     */
    private fun RefundOperationRequest.refundCardPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            stepWithSave(
                action = gpbCardRefundIntegration::refundPayCard,
                resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
            ),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, REFUND_INTERNAL_ERROR)
        } onFinalState {
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    /**
     * Проверяет доступность операции по правилу.
     */
    private fun RefundOperationRequest.checkAvailability(): RefundOperationRequest {
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
    private fun <RESULT> OperationCommand<RefundOperationRequest, RESULT>.runRefundCommand(): RESULT = operationService.runOperation(this)
}
