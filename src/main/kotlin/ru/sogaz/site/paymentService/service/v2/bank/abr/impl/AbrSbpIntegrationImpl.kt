package ru.sogaz.site.paymentService.service.v2.bank.abr.impl

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.abr.AbrSbpClient
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.request.AbrRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.response.AbrSbpResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.abr.AbrSbpProperties
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrSbpPayIntegration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class AbrSbpIntegrationImpl(
    private val abrSbpClient: AbrSbpClient,
    private val requestMapper: AbrRequestMapper,
    private val responseMapper: AbrSbpResponseMapper,
    private val abrSbpProperties: AbrSbpProperties,
) : AbrSbpPayIntegration {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    override fun sbpPay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData {
        val redirectUrl = sbpPayOperationRequest.params.urlToReturn ?: abrSbpProperties.redirectUrl
        val paymentResponse = requestMapper
            .toSbpPaymentRequest(sbpPayOperationRequest, redirectUrl, plus15MinutesFromNow())
            .run(abrSbpClient::pay)

        val paymentData = responseMapper.toBankPaymentPageData(paymentResponse)
        val paymentPass = paymentResponse.order.password

        abrSbpClient.setSrcToken(paymentData.paymentBankId, paymentPass, requestMapper.toSetSrcTokenRequest())
        val pushTranResponse = abrSbpClient.preparePushTran(
            paymentData.paymentBankId,
            paymentPass,
            requestMapper.toPreparePushTranRequest(redirectUrl),
        )

        return responseMapper.toSbpPaymentPageData(paymentData, pushTranResponse)
    }

    override fun payStatus(sbpPayOperation: SbpPayOperation): BankOperationDetails =
        throw UnsupportedOperationException("ABR sbp status not implemented yet")

    private fun plus15MinutesFromNow() = LocalDateTime.now().plusMinutes(15).format(formatter)
}
