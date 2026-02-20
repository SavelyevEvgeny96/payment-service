package ru.sogaz.site.paymentService.service.v2.operation.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
@Transactional(rollbackFor = [Exception::class])
class OperationServiceImpl(
    private val idempotentOrderService: IdempotentOrderService,
    private val payOperationServiceImpl: PayOperationServiceImpl,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
) : OperationService {
    override fun <R : PayOperationRequest> pay(payOperationRequest: R): BankPaymentPageData {
        val idempotentOrderOperation = idempotentOrderService.saveOperation(payOperationRequest)
        val paymentPageData =
            payOperationServiceImpl
                .payOperation(payOperationRequest)
                .execute()
        idempotentOrderService.updateOperation(idempotentOrderOperation, paymentPageData)
        checkOperationStatusProducer.sendDelayedCheckStatusEvent(idempotentOrderOperation)
        return paymentPageData
    }
}
