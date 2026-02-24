package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.operation.PayOperationRequestMapper
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService

@Service
class PayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardIntegration: GpbCardIntegration,
    private val payOperationRequestMapper: PayOperationRequestMapper,
) : PayOperationService {
    override fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .cardPayOperationCommand()
            .runCommand()

    private fun CardPayOperationRequest.cardPayOperationCommand() =
        OperationCommand(
            this,
            payOperationRequestMapper::toIdempotentOrderOperation,
            cardPayStrategy(),
        )

    private fun CardPayOperationRequest.cardPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
        ).stepWithSave(
            action = gpbCardIntegration::cardPay,
            resultMapper = payOperationRequestMapper::updateByBankPaymentPage,
        )

    override fun sbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentPageData = TODO()

    private fun <REQUEST : OperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.runCommand(): RESULT =
        operationService.runIdempotentOperation(this)
}
