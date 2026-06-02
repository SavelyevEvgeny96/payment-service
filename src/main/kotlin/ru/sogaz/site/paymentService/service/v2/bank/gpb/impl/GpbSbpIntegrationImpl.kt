package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbSbpResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpReversalPayOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpPayIntegration
import java.time.Instant

/**
 * Класс интеграции с Газпромбанком.
 * Реализует взаимодействие по регистрации и получении qr кода для оплаты по сбп, а также получении статуса оплаты по сбп.
 * - Запросы в банк собираются с использованием GpbRequestMapper
 * - Ответы от банка преобразовываются с использованием GpbSbpResponseMapper
 */
@Service
class GpbSbpIntegrationImpl(
    private val gpbSbpClient: GpbSbpClient,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbSbpResponseMapper,
    private val accountProperties: GpbSbpAccountProperties,
) : GpbSbpPayIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR = "Во время получения данных по операции СБП произошла ошибка: {}"
    }

    private val logger = loggerFor(javaClass)

    override fun sbpPay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        sbpPayOperationRequest
            .buildBankRequest()
            .sbpPay()
            .toBankPaymentPageData()

    private fun SbpPayOperationRequest.buildBankRequest(): GpbSbpPayRequest =
        requestMapper.toSbpRequest(
            this,
            accountProperties.account,
            params.urlToReturn ?: accountProperties.redirectUrl,
        )

    private fun GpbSbpPayRequest.sbpPay(): GpbSbpPayResponse = gpbSbpClient.pay(this)

    private fun GpbSbpPayResponse.toBankPaymentPageData() = responseMapper.toBankPaymentPageData(this)

    override fun payStatus(sbpPayOperation: SbpPayOperation): BankOperationDetails {
        try {
            if (sbpPayOperation.isExpired()) {
                return BankOperationDetails(sbpPayOperation.qrId, OperationState.FAIL)
            }
            val currentSbpBankState = getCurrentSbpBankState(sbpPayOperation)
            return BankOperationDetails(sbpPayOperation.qrId, currentSbpBankState)
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            return BankOperationDetails(sbpPayOperation.qrId, OperationState.WAIT)
        }
    }

    override fun payReversalStatus(sbpReversalPayOperation: SbpReversalPayOperation): BankOperationDetails {
        try {
            val currentSbpBankState = getCurrentSbpReversalBankState(sbpReversalPayOperation)
            return BankOperationDetails(sbpReversalPayOperation.paymentBankId, currentSbpBankState)
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            return BankOperationDetails(sbpReversalPayOperation.paymentBankId, OperationState.WAIT)
        }
    }

    private fun SbpPayOperation.isExpired(): Boolean {
        val secondsTtl = accountProperties.account.qrcTtl * 60
        val expiredTime = Instant.now().plusSeconds(secondsTtl.toLong())
        return operationStarted.isAfter(expiredTime)
    }

    private fun getCurrentSbpBankState(sbpPayOperation: SbpPayOperation): OperationState {
        val sbpStatusRequest = requestMapper.toSbpStatusRequest(sbpPayOperation)
        val sbpStatusResponse = gpbSbpClient.getStatus(sbpStatusRequest)
        val operationDetails = responseMapper.toBankOperationDetails(sbpStatusResponse.first())
        return operationDetails.state
    }

    private fun getCurrentSbpReversalBankState(sbpReversalPayOperation: SbpReversalPayOperation): OperationState {
        val sbpReversalStatusRequest = requestMapper.toSbpReversalStatusRequest(sbpReversalPayOperation)
        val sbpReversalStatusResponse = gpbSbpClient.getStatusReversal(sbpReversalStatusRequest)
        val operationDetails = responseMapper.toBankOperationDetailsReversal(sbpReversalStatusResponse)
        return operationDetails.state
    }

    override fun getQrContent(
        sbpPayOperationRequest: SbpPayOperationRequest,
        bankPaymentPageData: BankPaymentPageData,
    ): BankPaymentQrContent =
        bankPaymentPageData
            .run(requestMapper::toQrImageRequest)
            .run(gpbSbpClient::getQrImage)
            .run { responseMapper.toBankPaymentQrData(bankPaymentPageData, this) }
}
