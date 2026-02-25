package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardClient
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration

@Component
class GpbCardCardIntegrationImpl(
    private val gpbCardClient: GpbCardClient,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbCardResponseMapper,
    private val cardAccountProperties: GpbCardAccountProperties,
) : GpbCardIntegration {
    override fun authorize(payOperationRequest: PayOperationRequest): AuthorizedCardTrxData {
        val accountData = chooseAccountDataForOperation(payOperationRequest)
        val token = gpbCardClient.getToken(accountData.portalId).token
        return AuthorizedCardTrxData(token, accountData)
    }

    override fun cardPay(
        cardPayOperationRequest: CardPayOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankPaymentPageData =
        cardPayOperationRequest
            .buildBankRequest(authorizedCardTrxData)
            .cardPay(authorizedCardTrxData)
            .toResponse()

    override fun recurrentPay(
        cardRecurrentOperationRequest: CardRecurrentOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankOperationDetails =
        cardRecurrentOperationRequest
            .buildBankRequest(authorizedCardTrxData)
            .cardRecurrentPay(authorizedCardTrxData)
            .toResponse()

    override fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails {
        val accountData = chooseAccountDataForOperation(cardPayOperation)
        val cardPayDetails = gpbCardClient.getPaymentStatus(accountData.portalId, cardPayOperation.paymentBankId)
        return responseMapper.toBankPaymentDetails(cardPayDetails)
    }

    private fun CardRecurrentOperationRequest.buildBankRequest(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayRequest =
        requestMapper.toRecurrentRequest(
            authorizedCardTrxData.account.merchantId,
            this,
        )

    private fun CardPayOperationRequest.buildBankRequest(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayRequest =
        requestMapper.toCardRequest(
            authorizedCardTrxData.account.merchantId,
            this,
            params,
        )

    private fun GpbPayRequest.cardPay(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayCardResponse =
        gpbCardClient.cardPayment(
            authorizedCardTrxData.account.portalId,
            authorizedCardTrxData.token,
            this,
        )

    private fun GpbPayRequest.cardRecurrentPay(authorizedCardTrxData: AuthorizedCardTrxData): GpbCardPayDetailsResponse =
        gpbCardClient.cardRecurrentPayment(
            authorizedCardTrxData.account.portalId,
            authorizedCardTrxData.token,
            this,
        )

    private fun GpbPayCardResponse.toResponse() = responseMapper.toBankPayData(this)

    private fun GpbCardPayDetailsResponse.toResponse() = responseMapper.toBankPaymentDetails(this)

    private fun chooseAccountDataForOperation(payOperationRequest: PayOperationRequest): GpbCardAccountData =
        chooseAccountData(payOperationRequest.params.depersonalization)

    private fun chooseAccountDataForOperation(payOperation: PayOperation): GpbCardAccountData =
        chooseAccountData(payOperation.depersonalization)

    private fun chooseAccountData(depersonalization: Boolean): GpbCardAccountData =
        cardAccountProperties.main.butIf(depersonalization) { cardAccountProperties.depersonalized }
}
