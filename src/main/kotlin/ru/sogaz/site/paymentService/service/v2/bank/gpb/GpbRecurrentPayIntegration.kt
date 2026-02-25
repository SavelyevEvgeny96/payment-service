package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest

interface GpbRecurrentPayIntegration {
    fun authorize(payOperationRequest: PayOperationRequest): AuthorizedCardTrxData

    fun recurrentPay(
        cardRecurrentOperationRequest: CardRecurrentOperationRequest,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): BankOperationDetails

    fun payStatus(cardPayOperation: CardPayOperation): BankOperationDetails
}
