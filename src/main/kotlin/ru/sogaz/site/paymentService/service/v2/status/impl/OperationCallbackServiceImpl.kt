package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.callback.GpbCallbackMapper
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.exception.OperationNotFoundException
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.status.OperationCallbackService
import java.time.Instant
import java.util.UUID

@Service
@Transactional(rollbackFor = [Exception::class])
class OperationCallbackServiceImpl(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val operationDetailsProducer: OperationDetailsProducer,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
    private val gpbCallbackMapper: GpbCallbackMapper,
) : OperationCallbackService {
    override fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback) {
        val orderOperation = findOrderOperationOrThrow(UUID.fromString(gpbCardCallback.merch_id), gpbCardCallback.trx_id)
        if (orderOperation.state.isFinaleState()) {
            return
        }
        val operationDetails = gpbCallbackMapper.toBankOperationDetails(gpbCardCallback)
        if (operationDetails.state.isFinaleState()) {
            updateOperationStatus(orderOperation, operationDetails)
        }
    }

    fun updateOperationStatus(
        operation: IdempotentOrderOperation,
        operationDetails: BankOperationDetails,
    ) {
        operationDetailsProducer.sendOperationDetails(operation, operationDetails)
        operation.state = operationDetails.state
        operation.operationFinished = Instant.now()
        idempotentOrderOperationDao.save(operation)
    }

    fun findOrderOperationOrThrow(
        orderId: UUID,
        paymentBankId: String,
    ) = idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(orderId, paymentBankId)
        ?: throw OperationNotFoundException(orderId, paymentBankId)

    override fun updateByGpbSbpCallback(
        orderId: UUID,
        paymentBankId: String,
    ) {
        val orderOperation = findOrderOperationOrThrow(orderId, paymentBankId)
        checkOperationStatusProducer.sendCheckStatusEvent(orderOperation)
    }
}
