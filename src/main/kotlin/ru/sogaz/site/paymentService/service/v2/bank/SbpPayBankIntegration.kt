package ru.sogaz.site.paymentService.service.v2.bank

import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface SbpPayBankIntegration {
    fun pay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData
}
