package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpReversalClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbSbpReversalRequestMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpReversalResponse
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.reversal.ReversalOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpReversalIntegration

@Component
class GpbSbpReversalIntegrationImpl(
    private val reversalClient: GpbSbpReversalClient,
    private val accountProperties: GpbSbpAccountProperties,
    private val requestMapper: GpbSbpReversalRequestMapper,
) : GpbSbpReversalIntegration {
    override fun reversalPaySbp(request: ReversalOperationRequest): BankOperationDetails {
        val headers = requestMapper.toHeaders(accountProperties)
        val prepareRequest = requestMapper.toPrepareRequest(request, accountProperties)
        val prepareResponse = reversalClient.prepare(headers, prepareRequest)

        if (!prepareResponse.isSuccess()) {
            return BankOperationDetails(
                bankId = prepareResponse.transactionId,
                state = OperationState.FAIL,
                errorText = prepareResponse.message ?: "GPB SBP prepare failed with code=${prepareResponse.code}",
            )
        }

        val confirmRequest = requestMapper.toConfirmRequest(prepareResponse.transactionId!!)
        val confirmResponse = reversalClient.confirm(headers, confirmRequest)

        return if (confirmResponse.isSuccess()) {
            BankOperationDetails(
                bankId = confirmResponse.transactionId,
                state = OperationState.NEW,
            )
        } else {
            BankOperationDetails(
                bankId = prepareResponse.transactionId,
                state = OperationState.FAIL,
                errorText = confirmResponse.message ?: "GPB SBP confirm failed with code=${confirmResponse.code}",
            )
        }
    }

    private fun GpbSbpReversalResponse.toSuccess() = BankOperationDetails(bankId = transactionId, state = OperationState.NEW)

    private fun GpbSbpReversalResponse.toFail() =
        BankOperationDetails(bankId = transactionId, state = OperationState.FAIL, errorText = message)
}
