package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
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

@Service
class RefundPayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardRefundIntegration: GpbCardRefundIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
) : RefundPayOperationService {
    companion object {
        private const val REFUND_TYPE_ERROR = "Не поддерживаемый для возвратов тип платежа"
        private const val REFUND_INTERNAL_ERROR = "Платежная система недоступна"
    }

    override fun refundPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails =
        when (refundOperationRequest.paymentType) {
            PaymentType.CARD -> refundCardPayOperation(refundOperationRequest)
            else -> throw InnerException(getTraceId(), REFUND_TYPE_ERROR)
        }

    override fun refundCardPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails =
        refundOperationRequest
            .refundCardPayOperationCommand()
            .runRefundCommand()

    private fun RefundOperationRequest.refundCardPayOperationCommand() =
        gpbOperationCommand(
            stepWithSave(
                action = gpbCardRefundIntegration::refundPayCard,
                resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
            ),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, REFUND_INTERNAL_ERROR)
        } onFinalState {
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    private fun <RESULT> OperationCommand<RefundOperationRequest, RESULT>.runRefundCommand(): RESULT =
        operationService.runOperation(this).getOrThrow()
}
