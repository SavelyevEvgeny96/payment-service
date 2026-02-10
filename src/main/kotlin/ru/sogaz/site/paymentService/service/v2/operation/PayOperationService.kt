package ru.sogaz.site.paymentService.service.v2.operation

import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.service.v2.strategy.pay.PayStrategy

interface PayOperationService {
    fun payOperation(payOperationRequest: PayOperationRequest): PayStrategy
}
