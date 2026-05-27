package ru.sogaz.site.paymentService.service.v2.pay

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails

interface ReversalPayOperationService {
    fun reversalPayOperation(
        reversalOperationRequest: ru.sogaz.site.paymentService.model.v2.web.reversal.ReversalOperationRequest,
    ): BankOperationDetails
}
