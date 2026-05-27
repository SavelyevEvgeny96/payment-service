package ru.sogaz.site.paymentService.service.v2.pay

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest

interface ReversalPayOperationService {
    fun reversalPayOperation(reversalOperationRequest: ReversalOperationRequest): BankOperationDetails
}
