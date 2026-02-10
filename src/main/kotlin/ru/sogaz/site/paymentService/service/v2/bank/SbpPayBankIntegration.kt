package ru.sogaz.site.paymentService.service.v2.bank

import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest

interface SbpPayBankIntegration {
    fun pay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData
}
