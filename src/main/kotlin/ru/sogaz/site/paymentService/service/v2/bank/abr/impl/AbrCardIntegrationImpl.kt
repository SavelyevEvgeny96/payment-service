package ru.sogaz.site.paymentService.service.v2.bank.abr.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.abr.AbrCardClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.request.AbrRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.response.AbrResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrCardPayIntegration
import ru.sogaz.site.paymentService.service.v2.bank.abr.redirect.AbrRedirectAddressService

@Service
class AbrCardIntegrationImpl(
    private val abrCardClient: AbrCardClient,
    private val requestMapper: AbrRequestMapper,
    private val responseMapper: AbrResponseMapper,
    private val abrRedirectAddressService: AbrRedirectAddressService,
) : AbrCardPayIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR = "Во время получения данных по операции оплаты картой АБР произошла ошибка: {}"
    }

    private val logger = loggerFor(javaClass)

    override fun cardPay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        cardPayOperationRequest
            .let { requestMapper.toCardRequest(it, abrRedirectAddressService.createStateRedirectUrl(it.params)) }
            .run(abrCardClient::cardPayment)
            .run(responseMapper::toCardPaymentPageData)

    override fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails =
        try {
            val statusResponse = abrCardClient.getPaymentStatus(
                cardPayOperation.paymentBankId,
                cardPayOperation.paymentPass,
            )
            responseMapper.toBankOperationDetails(statusResponse)
        } catch (ex: Exception) {
            logger.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            BankOperationDetails(cardPayOperation.paymentBankId, OperationState.WAIT)
        }
}
