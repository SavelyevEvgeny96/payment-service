package ru.sogaz.site.paymentService.service.v2.bank.abr.impl

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.abr.AbrCardClient
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.request.AbrRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.response.AbrCardResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrCardIntegration

@Component
class AbrCardIntegrationImpl(
    private val abrCardClient: AbrCardClient,
    private val requestMapper: AbrRequestMapper,
    private val responseMapper: AbrCardResponseMapper,
    private val apiConfigProperties: ApiConfigProperties,
) : AbrCardIntegration {
    override fun cardPay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData {
        val redirectUrl = cardPayOperationRequest.params.urlToReturnS ?: apiConfigProperties.backUrlS
        return requestMapper
            .toCardPaymentRequest(cardPayOperationRequest, redirectUrl)
            .run(abrCardClient::pay)
            .run(responseMapper::toBankPaymentPageData)
    }

    override fun payStatus(payOperation: PayOperation): BankOperationDetails =
        throw UnsupportedOperationException("ABR status requires payment password and is not wired in v2 yet")
}
