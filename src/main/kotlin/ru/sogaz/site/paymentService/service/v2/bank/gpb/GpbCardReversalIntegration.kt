package ru.sogaz.site.paymentService.service.v2.bank.gpb

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest

interface GpbCardReversalIntegration {
    fun reversalPayCard(request: ReversalOperationRequest): BankOperationDetails
}
