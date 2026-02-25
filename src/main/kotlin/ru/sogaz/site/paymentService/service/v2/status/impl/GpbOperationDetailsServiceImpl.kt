package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.operation.OperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration
import ru.sogaz.site.paymentService.service.v2.status.OperationDetailsService

@Service
class GpbOperationDetailsServiceImpl(
    private val operationMapper: OperationMapper,
    private val gpbCardIntegration: GpbCardIntegration,
) : OperationDetailsService {
    override fun getOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails =
        when (idempotentOrderOperation.operationType) {
            OperationType.PAY -> getPayOperationDetails(idempotentOrderOperation)
            OperationType.RECURRENT -> TODO()
            OperationType.CARD_REGISTRATION -> TODO()
        }

    private fun getPayOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails =
        when (val payOperation = operationMapper.makePayOperation(idempotentOrderOperation)) {
            is CardPayOperation -> gpbCardIntegration.payStatus(payOperation)
        }
}
