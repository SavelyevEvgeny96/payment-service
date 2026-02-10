package ru.sogaz.site.paymentService.service.v2.status

import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

interface OperationDetailsService {
    fun getOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails
}
