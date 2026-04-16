package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import feign.FeignException
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardAuthClient
import ru.sogaz.site.paymentService.clients.gpb.GpbCardClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCardResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.exception.GpbCardPayErrorMessage
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.emptyCardDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardAccountManager
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration

/**
 * Класс интеграции с Газпромбанком.
 * Реализует взаимодействие по оплате картой и рекуррентной оплате картой, а также получении статусов оплат картой.
 * - Для оплаты регистрирует токен с использованием аккаунта полученного от GpbCardAccountManager.
 * - Запросы в банк собираются с использованием GpbRequestMapper
 * - Ответы от банка преобразовываются с использованием GpbCardResponseMapper
 */
@Component
class GpbCardIntegrationImpl(
    private val gpbCardClient: GpbCardClient,
    private val gpbCardAuthClient: GpbCardAuthClient,
    private val requestMapper: GpbRequestMapper,
    private val responseMapper: GpbCardResponseMapper,
    private val objectMapper: ObjectMapper,
    private val cardAccountManager: GpbCardAccountManager,
) : GpbCardIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR =
            "Во время получения данных по операции оплаты картой произошла ошибка: {}"
        private const val CARD_NOT_FOUND = "Карта не найдена"
        private const val TRANSACTION_NOT_STARTED = "Транзакция по этой операции не была открыта"
        private const val BAD_REQUEST = "Bad request"
        private const val INTERNAL_ERROR = "Internal server error"
    }

    private val logger = loggerFor(javaClass)

    override fun authorize(payOperationRequest: PayOperationRequest): AuthorizedCardTrxData {
        val accountData = cardAccountManager.getByOperation(payOperationRequest)
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

    override fun regPay(
        payRegOperationRequest: PayRegOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankPaymentPageData =
        payRegOperationRequest
            .buildRegPayRequest(authorizedCardTrxData)
            .cardPay(authorizedCardTrxData)
            .toBankPaymentPageData()

    private fun PayRegOperationRequest.buildRegPayRequest(authorizedCardTrxData: AuthorizedCardTrxData): GpbPayRequest =
        requestMapper.toRegRequest(
            authorizedCardTrxData.account.merchantId,
            this,
            params,
        )

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
            ex.toFailOperationDetails(
                authorizedCardTrxData.token,
                cardRecurrentOperationRequest.keyCard,
            ) { CARD_NOT_FOUND }
        } catch (ex: FeignException.BadRequest) {
            ex.toFailOperationDetails(authorizedCardTrxData.token) { BAD_REQUEST }
        }

    private fun FeignException.toFailOperationDetails(
        bankId: String,
        defaultError: () -> String = { INTERNAL_ERROR },
    ): BankOperationDetails = BankOperationDetails(bankId, OperationState.FAIL, errorText = getErrorCode() ?: defaultError())

    private fun FeignException.toFailOperationDetails(
        bankId: String,
        keyCard: String,
        defaultError: () -> String = { INTERNAL_ERROR },
    ): BankOperationDetails =
        BankOperationDetails(
            bankId,
            OperationState.FAIL,
            cardDetails = emptyCardDetails(keyCard),
            errorText = getErrorCode() ?: defaultError(),
        )

    private fun FeignException.getErrorCode(): String? =
        runCatching { objectMapper.readValue<GpbCardPayErrorMessage>(contentUTF8()).error?.message }
            .onFailure { ex -> logger.error(ex.message, ex) }
            .getOrNull()

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
            val accountData = cardAccountManager.getByOperation(cardPayOperation)
            val cardPayDetails = gpbCardClient.getPaymentStatus(accountData.portalId, cardPayOperation.paymentBankId)
            responseMapper.toBankPaymentDetails(cardPayDetails)
        } catch (ex: FeignException.NotFound) {
            ex.toFailOperationDetails(cardPayOperation.paymentBankId) { TRANSACTION_NOT_STARTED }
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            BankOperationDetails(cardPayOperation.paymentBankId, OperationState.WAIT)
        }
}
