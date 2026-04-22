package ru.sogaz.site.paymentService.service.v2.bank.abr

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.PayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface AbrCardPayIntegration {
    fun cardPay(cardPayOperationRequest: CardPayOperationRequest): BankPaymentPageData

    fun payStatus(payOperation: PayOperation): BankOperationDetails
}
