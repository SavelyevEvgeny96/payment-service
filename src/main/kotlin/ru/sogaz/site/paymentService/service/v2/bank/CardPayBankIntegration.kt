package ru.sogaz.site.paymentService.service.v2.bank

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface CardPayBankIntegration {
    fun pay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData

    fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails
}
