package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface GpbCardPayIntegration {
    fun authorize(payOperationRequest: PayOperationRequest): AuthorizedCardTrxData

    fun cardPay(
        cardPayOperationRequest: CardPayOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankPaymentPageData

    fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails
}
