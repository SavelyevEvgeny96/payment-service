package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpAdminClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbSbpAdminAutoPayRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbSbpResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbSbpAutoPayHeader
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAutoPayHeaders
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GpbSbpAdminAutoPayOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSpbAutoPayIntegration

@Service
@Profile(value = ["test", "local", "stage"])
class GpbSbpAutoPayIntegrationImpl(
    private val gpbSbpClient: GpbSbpAdminClient,
    private val requestMapper: GpbSbpAdminAutoPayRequestMapper,
    private val responseMapper: GpbSbpResponseMapper,
    private val accountProperties: GpbSbpAccountProperties,
) : GpbSpbAutoPayIntegration {
    override fun sbpAdminAutoPay(sbpPayOperationRequest: GpbSbpAdminAutoPayOperationRequest) =
        sbpPayOperationRequest
            .buildBankRequest()
            .sbpPay(sbpPayOperationRequest.headers)
            .toBankPaymentPageData()

    private fun GpbSbpAdminAutoPayOperationRequest.buildBankRequest(): GpbSbpPayRequest =
        requestMapper.toSbpRequest(this, accountProperties.account)

    private fun GpbSbpPayRequest.sbpPay(headers: GpbSbpAutoPayHeaders): GpbSbpPayResponse = gpbSbpClient.autoPay(sbpHeaders(headers), this)

    private fun sbpHeaders(headers: GpbSbpAutoPayHeaders) =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set(GpbSbpAutoPayHeader.PAYMENT_DELAY.value, headers.paymentDelay)
            set(GpbSbpAutoPayHeader.PROCESS_PAYMENTS.value, headers.processPayments)
            set(GpbSbpAutoPayHeader.PAYMENT_STATUS.value, headers.paymentStatus)
        }

    private fun GpbSbpPayResponse.toBankPaymentPageData() = responseMapper.toBankPaymentPageData(this)
}
