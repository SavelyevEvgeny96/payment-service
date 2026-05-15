package ru.sogaz.site.paymentService.service.v2.order.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.reversal.ReversalOperationRequest
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

/**
 * Сервис реализующий работу с записями банковских операций в базе.
 */
@Service
@Transactional
class IdempotentOrderServiceImpl(
    private val idempotentOrderDao: IdempotentOrderDao,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
) : IdempotentOrderService {
    override fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation = saveOperation(operationRequest, bank, null)

    override fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
        operationMapper: (R.() -> IdempotentOrderOperation)?,
    ): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(operationRequest)
        val idempotentOrderOperation = operationRequest.mapToOperation(operationMapper)
        return idempotentOrderOperation
            .apply {
                this.idempotentOrder = idempotentOrder
                this.bank = bank
            }.run(idempotentOrderOperationDao::save)
    }

    private fun findIdempotentOrderOrCreateNewOne(operationRequest: OperationRequest): IdempotentOrder =
        idempotentOrderDao.findIdempotentOrderByOrderId(operationRequest.orderId)
            ?: saveNewOrder(operationRequest)

    private fun saveNewOrder(operationRequest: OperationRequest): IdempotentOrder =
        operationRequest
            .run(idempotentOrderOperationMapper::toIdempotentOrder)
            .run(idempotentOrderDao::save)

    private fun <R : OperationRequest> R.mapToOperation(
        mapToIdempotentOrderOperation: (R.() -> IdempotentOrderOperation)?,
    ): IdempotentOrderOperation =
        mapToIdempotentOrderOperation?.invoke(this)
            ?: idempotentOrderOperationMapper.toIdempotentOrderOperation(this)

    override fun findOperation(refundRequest: ReversalOperationRequest): IdempotentOrderOperation? =
        idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(refundRequest.orderId, refundRequest.paymentBankId)

    override fun saveOperation(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation =
        idempotentOrderOperationDao.save(idempotentOrderOperation)
}
