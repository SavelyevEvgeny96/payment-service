package ru.sogaz.site.paymentService.service.v2.bank.abr.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.abr.AbrSbpClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.request.AbrRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.response.AbrResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrSbpPayIntegration

@Service
class AbrSbpIntegrationImpl(
    private val abrSbpClient: AbrSbpClient,
    private val requestMapper: AbrRequestMapper,
    private val responseMapper: AbrResponseMapper,
) : AbrSbpPayIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR = "Во время получения данных по операции СБП АБР произошла ошибка: {}"
    }

    private val logger = loggerFor(javaClass)

    override fun sbpPay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData {
        val orderResponse =
            sbpPayOperationRequest
                .run(requestMapper::toSbpRequest)
                .run(abrSbpClient::sbpPayment)
        val paymentBankId = orderResponse.order.id.toString()
        val paymentPass = orderResponse.order.password

        abrSbpClient.setSrcToken(paymentBankId, paymentPass, requestMapper.toSetSrcTokenRequest())
        val pushTranResponse =
            abrSbpClient.preparePushTran(
                paymentBankId,
                paymentPass,
                requestMapper.toPreparePushTranRequest(sbpPayOperationRequest.params),
            )

        return responseMapper.toSbpPaymentPageData(orderResponse, pushTranResponse)
    }

    override fun payStatus(sbpPayOperation: SbpPayOperation): BankOperationDetails =
        try {
            val statusResponse =
                abrSbpClient.getPaymentStatus(
                    sbpPayOperation.paymentBankId,
                    sbpPayOperation.paymentPass,
                )
            responseMapper.toBankOperationDetails(statusResponse)
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            BankOperationDetails(sbpPayOperation.paymentBankId, OperationState.WAIT)
        }
}
