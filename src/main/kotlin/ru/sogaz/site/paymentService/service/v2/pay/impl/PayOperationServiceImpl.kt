package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpPayIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.gpbOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFailure
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFinalState
import ru.sogaz.site.paymentService.service.v2.operation.inline.step
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService

@Service
class PayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardIntegration: GpbCardIntegration,
    private val gpbSbpIntegration: GpbSbpPayIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
) : PayOperationService {
    companion object {
        private const val RECURRENT_INTERNAL_ERROR = "Платежная система недоступна"
    }
    override fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .cardPayOperationCommand()
            .runCommand()

    private fun CardPayOperationRequest.cardPayOperationCommand() =
        gpbOperationCommand(
            strategy = cardPayStrategy(),
        )

    private fun CardPayOperationRequest.cardPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).stepWithSave(
            action = gpbCardIntegration::cardPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        )

    override fun sbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .sbpPayOperationCommand()
            .runCommand()

    private fun SbpPayOperationRequest.sbpPayOperationCommand() =
        gpbOperationCommand(
            strategy = sbpPayStrategy(),
        )

    private fun SbpPayOperationRequest.sbpPayStrategy() =
        stepWithSave(
            action = gpbSbpIntegration::sbpPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        )

    override fun recurrentOperation(recurrentOperationRequest: CardRecurrentOperationRequest): BankOperationDetails =
        recurrentOperationRequest
            .cardRecurrentPayOperationCommand()
            .runCommand()

    private fun CardRecurrentOperationRequest.cardRecurrentPayOperationCommand() =
        gpbOperationCommand(
            strategy = cardRecurrentPayStrategy(),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, RECURRENT_INTERNAL_ERROR)
        } onFinalState {
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    private fun CardRecurrentOperationRequest.cardRecurrentPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).stepWithSave(
            action = gpbCardIntegration::recurrentPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
        )

    override fun qrImageSbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentQrContent =
        payOperationRequest
            .qrImageSbpPayOperationCommand()
            .runCommand()

    private fun SbpPayOperationRequest.qrImageSbpPayOperationCommand() =
        gpbOperationCommand(
            strategy = qrImageSbpPayStrategy(),
        )

    private fun SbpPayOperationRequest.qrImageSbpPayStrategy() =
        stepWithSave(
            action = gpbSbpIntegration::sbpPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        ).step(
            action = gpbSbpIntegration::getQrContent,
        )

    private fun <REQUEST : PayOperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.runCommand(): RESULT =
        operationService.runOperation(this).getOrThrow()
}
