package ru.sogaz.site.paymentService.service.v2.operation

import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest

interface OperationService {
    fun <R : PayOperationRequest> pay(payOperationRequest: R): BankPaymentPageData
}
