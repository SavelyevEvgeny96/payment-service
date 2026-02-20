package ru.sogaz.site.paymentService.service.v2.strategy.pay.gpb

import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.bank.CardPayBankIntegration
import ru.sogaz.site.paymentService.service.v2.strategy.pay.PayStrategy

class CardPayStrategy(
    private val request: CardPayOperationRequest,
    private val cardPayBankIntegration: CardPayBankIntegration,
) : PayStrategy {
    override fun execute(): BankPaymentPageData = cardPayBankIntegration.pay(request)
}
