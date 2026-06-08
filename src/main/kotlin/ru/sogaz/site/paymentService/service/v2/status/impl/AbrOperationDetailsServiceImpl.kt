package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.mapper.v2.operation.OperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.core.pay.CardPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpPayOperation
import ru.sogaz.site.paymentService.model.v2.core.pay.SbpReversalPayOperation
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrCardPayIntegration
import ru.sogaz.site.paymentService.service.v2.bank.abr.AbrSbpPayIntegration
import ru.sogaz.site.paymentService.service.v2.status.OperationDetailsService

@Service
class AbrOperationDetailsServiceImpl(
    private val operationMapper: OperationMapper,
    private val abrCardIntegration: AbrCardPayIntegration,
    private val abrSbpIntegration: AbrSbpPayIntegration,
) : OperationDetailsService {
    companion object {
        private const val ABR_SBP_REVERSAL_NOT_AVAILABLE = "Операция возврата СБП через АБР недоступна"
    }

    override fun getOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails =
        when (idempotentOrderOperation.operationType) {
            OperationType.PAY -> getPayOperationDetails(idempotentOrderOperation)
            OperationType.RECURRENT -> getPayOperationDetails(idempotentOrderOperation)
            OperationType.REGISTRATION -> getPayOperationDetails(idempotentOrderOperation)
            OperationType.REVERSAL -> getPayOperationDetails(idempotentOrderOperation)
        }

    private fun getPayOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails =
        when (val payOperation = operationMapper.makePayOperation(idempotentOrderOperation)) {
            is CardPayOperation -> abrCardIntegration.payStatus(payOperation)
            is SbpPayOperation -> abrSbpIntegration.payStatus(payOperation)
            is SbpReversalPayOperation -> throw InnerException(getTraceId(), ABR_SBP_REVERSAL_NOT_AVAILABLE)
        }
}
