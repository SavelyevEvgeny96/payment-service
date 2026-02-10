package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.service.v2.status.OperationDetailsService

@Service("operationDetailsService")
class OperationDetailsServiceImpl(
    private val gpbOperationStatusServiceImpl: GpbOperationDetailsServiceImpl,
) : OperationDetailsService {
    companion object {
        private const val EMPTY_BANK = "Для операции не указан банк"
    }

    override fun getOperationDetails(idempotentOrderOperation: IdempotentOrderOperation): BankOperationDetails =
        when (idempotentOrderOperation.bank) {
            BankEnum.GPB -> gpbOperationStatusServiceImpl.getOperationDetails(idempotentOrderOperation)
            BankEnum.AKB_RUS -> TODO()
            else -> throw InnerException(getTraceId(), EMPTY_BANK)
        }
}
