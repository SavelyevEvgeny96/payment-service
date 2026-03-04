package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpPayIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.bankOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFinalState
import ru.sogaz.site.paymentService.service.v2.operation.inline.step
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService
import ru.sogaz.site.paymentService.service.v2.status.OperationStatusUpdater

@Service
class PayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardIntegration: GpbCardIntegration,
    private val gpbSbpIntegration: GpbSbpPayIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationStatusUpdater: OperationStatusUpdater,
) : PayOperationService {
    override fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .cardPayOperationCommand()
            .runCommand()

    private fun CardPayOperationRequest.cardPayOperationCommand() =
        bankOperationCommand(
            requestToOrderOperationMapper = idempotentOrderOperationMapper::toGpbIdempotentOrderOperation,
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
        bankOperationCommand(
            requestToOrderOperationMapper = idempotentOrderOperationMapper::toGpbIdempotentOrderOperation,
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
        OperationCommand(
            request = this,
            requestToOrderOperationMapper = idempotentOrderOperationMapper::toGpbIdempotentOrderOperation,
            strategy = cardRecurrentPayStrategy(),
        ).onFinalState(
            operationStatusUpdater::updateByOperationDetails,
        )

    private fun CardRecurrentOperationRequest.cardRecurrentPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).step(
            action = gpbCardIntegration::recurrentPay,
        )

    private fun <REQUEST : OperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.runCommand(): RESULT =
        operationService.runIdempotentOperation(this).getOrThrow()
}
