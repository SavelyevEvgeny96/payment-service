package ru.sogaz.site.paymentService.service.v2.bank.abr

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface AbrSbpPayIntegration {
    fun sbpPay(sbpPayOperationRequest: SbpPayOperationRequest): BankPaymentPageData

    fun payStatus(sbpPayOperation: SbpPayOperation): BankOperationDetails
}
