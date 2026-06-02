package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.status.OperationStatusUpdater

@Service
class OperationStatusUpdaterImpl(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val operationDetailsProducer: OperationDetailsProducer,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
) : OperationStatusUpdater {
    override fun updateByOperationDetails(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ): IdempotentOrderOperation {
        if (operationDetails.state.isFinaleState().not()) {
            return operation
        }
        operationDetailsProducer.sendOperationDetails(operation, operationDetails)
        idempotentOrderOperationMapper.updateByBankOperationDetails(operation, operationDetails)
        return idempotentOrderOperationDao.save(operation)
    }
}
