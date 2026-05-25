package ru.sogaz.site.paymentService.service.v2.pay

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest

interface RefundPayOperationService {
    fun refundCardPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails

    fun refundPayOperation(refundOperationRequest: RefundOperationRequest): BankOperationDetails
}
