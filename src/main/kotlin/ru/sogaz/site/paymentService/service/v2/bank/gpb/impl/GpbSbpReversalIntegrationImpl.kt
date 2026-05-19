package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpReversalClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbSbpReversalRequestMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpReversalResponse
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpReversalIntegration

@Component
class GpbSbpReversalIntegrationImpl(
    private val reversalClient: GpbSbpReversalClient,
    private val accountProperties: GpbSbpAccountProperties,
    private val requestMapper: GpbSbpReversalRequestMapper,
) : GpbSbpReversalIntegration {

    override fun reversalPaySbp(request: RefundOperationRequest): BankOperationDetails {
        val headers = requestMapper.toHeaders(accountProperties)
        val prepareRequest = requestMapper.toPrepareRequest(request, accountProperties)
        val prepareResponse = reversalClient.prepare(headers, prepareRequest)

        if (!prepareResponse.isSuccess()) return prepareResponse.toFail()

        val confirmRequest = requestMapper.toConfirmRequest(prepareResponse.transactionId)
        val confirmResponse = reversalClient.confirm(headers, confirmRequest)

        return if (confirmResponse.isSuccess()) confirmResponse.toSuccess() else confirmResponse.toFail()
    }

    private fun GpbSbpReversalResponse.toSuccess() = BankOperationDetails(bankId = transactionId, state = OperationState.SUCCESS)

    private fun GpbSbpReversalResponse.toFail() =
        BankOperationDetails(bankId = transactionId, state = OperationState.FAIL, errorText = message)
}
