package ru.sogaz.site.paymentService.service.v2.bank.gpb

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClientV2
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.GpbResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.properties.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.CardPayBankIntegration

@Component
class GpbCardPayIntegration(
    private val gpbCardClient: GpbCardPaymentClientV2,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbResponseMapper,
    private val cardAccountProperties: GpbCardAccountProperties,
) : CardPayBankIntegration {
    override fun pay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        pay(
            cardPayOperationRequest,
            cardAccountProperties.main.butIf(cardPayOperationRequest.params.depersonalization) { cardAccountProperties.depersonalized },
        )

    private fun pay(
        cardPayOperationRequest: CardPayOperationRequest,
        cardAccountData: GpbCardAccountData,
    ): BankPaymentPageData {
        val token = gpbCardClient.getToken(cardAccountData.portalId).token
        val request = requestMapper.toCardRequest(cardAccountData.merchantId, cardPayOperationRequest, cardPayOperationRequest.params)
        val response = gpbCardClient.cardPayment(cardAccountData.portalId, token, request)
        return responseMapper.toBankPayData(response)
    }

    override fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails =
        payStatus(
            cardPayOperation,
            cardAccountProperties.main.butIf(cardPayOperation.depersonalization) { cardAccountProperties.depersonalized },
        )

    private fun payStatus(
        cardPayOperation: CardPayOperation,
        cardAccountData: GpbCardAccountData,
    ): BankOperationDetails {
        val cardPayDetails = gpbCardClient.getPaymentStatus(cardAccountData.portalId, cardPayOperation.paymentBankId)
        return responseMapper.toBankPaymentDetails(cardPayDetails)
    }
}
