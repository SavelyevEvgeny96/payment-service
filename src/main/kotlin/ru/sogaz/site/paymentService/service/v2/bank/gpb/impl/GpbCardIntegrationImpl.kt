package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import feign.FeignException
import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardAuthClient
import ru.sogaz.site.paymentService.clients.gpb.GpbCardClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.exception.GpbCardPayErrorMessage
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbCardAccountProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration

@Component
class GpbCardIntegrationImpl(
    private val gpbCardClient: GpbCardClient,
    private val gpbCardAuthClient: GpbCardAuthClient,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbCardResponseMapper,
    private val objectMapper: ObjectMapper,
    private val cardAccountProperties: GpbCardAccountProperties,
) : GpbCardIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR = "Во время получения данных по операции оплаты картой произошла ошибка: {}"
        private const val CARD_NOT_FOUND = "Карта не найдена"
        private const val BAD_REQUEST = "Bad request"
    }

    private val logger = loggerFor(javaClass)

    override fun authorize(payOperationRequest: PayOperationRequest): AuthorizedCardTrxData {
        val accountData = chooseAccountDataForOperation(payOperationRequest)
        val token = gpbCardAuthClient.getToken(accountData.portalId).token
        return AuthorizedCardTrxData(token, accountData)
    }

    override fun cardPay(
        cardPayOperationRequest: CardPayOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankPaymentPageData =
        cardPayOperationRequest
            .buildCardPayRequest(authorizedCardTrxData)
            .cardPay(authorizedCardTrxData)
            .toBankPaymentPageData()

    private fun CardPayOperationRequest.buildCardPayRequest(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayRequest =
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

    private fun GpbPayCardResponse.toBankPaymentPageData() = responseMapper.toBankPaymentPageData(this)

    override fun recurrentPay(
        cardRecurrentOperationRequest: CardRecurrentOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankOperationDetails =
        try {
            cardRecurrentOperationRequest
                .buildRecurrentRequest(authorizedCardTrxData)
                .cardRecurrentPay(authorizedCardTrxData)
                .toBankPaymentPageData()
        } catch (ex: FeignException.NotFound) {
            BankOperationDetails(authorizedCardTrxData.token, OperationState.FAIL, errorText = ex.getErrorCode() ?: CARD_NOT_FOUND)
        } catch (ex: FeignException.BadRequest) {
            BankOperationDetails(authorizedCardTrxData.token, OperationState.FAIL, errorText = ex.getErrorCode() ?: BAD_REQUEST)
        }

    private fun FeignException.getErrorCode(): String? =
        try {
            objectMapper.readValue<GpbCardPayErrorMessage>(contentUTF8()).error?.message
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            null
        }

    private fun CardRecurrentOperationRequest.buildRecurrentRequest(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayRequest =
        requestMapper.toRecurrentRequest(
            authorizedCardTrxData.account.merchantId,
            this,
        )

    private fun GpbPayRequest.cardRecurrentPay(authorizedCardTrxData: AuthorizedCardTrxData): GpbCardPayDetailsResponse =
        gpbCardClient.cardRecurrentPayment(
            authorizedCardTrxData.account.portalId,
            authorizedCardTrxData.token,
            this,
        )

    private fun GpbCardPayDetailsResponse.toBankPaymentPageData() = responseMapper.toBankPaymentDetails(this)

    override fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails =
        try {
            val accountData = chooseAccountDataForOperation(cardPayOperation)
            val cardPayDetails = gpbCardClient.getPaymentStatus(accountData.portalId, cardPayOperation.paymentBankId)
            responseMapper.toBankPaymentDetails(cardPayDetails)
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            when (ex) {
                is FeignException.NotFound ->
                    BankOperationDetails(
                        cardPayOperation.paymentBankId,
                        OperationState.FAIL,
                        errorText = ex.getErrorCode(),
                    )
                else -> BankOperationDetails(cardPayOperation.paymentBankId, OperationState.WAIT)
            }
        }

    private fun chooseAccountDataForOperation(payOperationRequest: PayOperationRequest): GpbCardAccountData =
        chooseAccountData(payOperationRequest.depersonalization)

    private fun chooseAccountDataForOperation(payOperation: PayOperation): GpbCardAccountData =
        chooseAccountData(payOperation.depersonalization)

    private fun chooseAccountData(depersonalization: Boolean): GpbCardAccountData =
        cardAccountProperties.main.butIf(depersonalization) { cardAccountProperties.depersonalized }
}
