package ru.sogaz.site.paymentService.service.v2.order

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

interface IdempotentOrderService {
    fun saveOperation(payOperationRequest: PayOperationRequest): IdempotentOrderOperation

    fun updateOperation(
        idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation
}
