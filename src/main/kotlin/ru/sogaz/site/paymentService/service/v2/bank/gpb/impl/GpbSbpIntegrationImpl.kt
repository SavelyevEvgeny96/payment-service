package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbSbpResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpPayIntegration

@Service
class GpbSbpIntegrationImpl(
    private val gpbSbpClient: GpbSbpClient,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbSbpResponseMapper,
    private val accountProperties: GpbSbpAccountProperties,
) : GpbSbpPayIntegration {
    override fun sbpPay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        sbpPayOperationRequest
            .buildBankRequest()
            .sbpPay()
            .toBankPaymentPageData()

    private fun SbpPayOperationRequest.buildBankRequest(): GpbSbpPayRequest = requestMapper.toSbpRequest(this, accountProperties.account)

    private fun GpbSbpPayRequest.sbpPay(): GpbSbpPayResponse = gpbSbpClient.pay(this)

    private fun GpbSbpPayResponse.toBankPaymentPageData() = responseMapper.toBankPaymentPageData(this)

    override fun payStatus(sbpPayOperation: SbpPayOperation): BankOperationDetails =
        requestMapper
            .toSbpStatusRequest(sbpPayOperation)
            .run(gpbSbpClient::getStatus)
            .firstOrNull()
            .run(responseMapper::toBankPaymentDetails)
}
