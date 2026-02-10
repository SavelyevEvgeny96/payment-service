package ru.sogaz.site.paymentService.service.v2.operation.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentPageData
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
@Transactional(rollbackFor = [Exception::class])
class OperationServiceImpl(
    val idempotentOrderService: IdempotentOrderService,
    val payOperationServiceImpl: PayOperationServiceImpl,
) : OperationService {
    override fun <R : PayOperationRequest> pay(payOperationRequest: R): BankPaymentPageData {
        val idempotentOrderOperation = idempotentOrderService.saveOperation(payOperationRequest)
        val paymentPageData =
            payOperationServiceImpl
                .payOperation(payOperationRequest)
                .execute()
        idempotentOrderService.updateOperation(idempotentOrderOperation, paymentPageData)
        return paymentPageData
    }
}
