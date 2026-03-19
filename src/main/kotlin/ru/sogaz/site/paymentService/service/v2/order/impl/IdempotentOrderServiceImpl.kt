package ru.sogaz.site.paymentService.service.v2.order.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
class IdempotentOrderServiceImpl(
    private val idempotentOrderDao: IdempotentOrderDao,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
) : IdempotentOrderService {
    override fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(operationRequest)
        val operation =
            when (operationRequest) {
                is PayOperationRequest -> idempotentOrderOperationMapper.toIdempotentOrderOperation(idempotentOrder, operationRequest, bank)
                is RefundOperationRequest -> idempotentOrderOperationMapper.toIdempotentOrderOperation(idempotentOrder, operationRequest)
                else -> idempotentOrderOperationMapper.toIdempotentOrderOperation(idempotentOrder, operationRequest, bank)
            }
        return idempotentOrderOperationDao.save(operation)
    }

    override fun <R : PayOperationRequest> saveOperation(
        operationRequest: R,
        bank: OperationBank,
    ): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(operationRequest)
        val operation = idempotentOrderOperationMapper.toIdempotentOrderOperation(idempotentOrder, operationRequest, bank)
        return idempotentOrderOperationDao.save(operation)
    }

    override fun saveOperation(operationRequest: RefundOperationRequest): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(operationRequest)
        val operation = idempotentOrderOperationMapper.toIdempotentOrderOperation(idempotentOrder, operationRequest)
        return idempotentOrderOperationDao.save(operation)
    }

    override fun findOperation(refundRequest: RefundOperationRequest): IdempotentOrderOperation? =
        idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(refundRequest.orderId, refundRequest.paymentBankId)

    override fun <R : OperationRequest> saveOperation(
        operationRequest: R,
        mapToIdempotentOrderOperation: R.() -> IdempotentOrderOperation,
    ): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(operationRequest)
        return operationRequest
            .mapToIdempotentOrderOperation()
            .apply { this.idempotentOrder = idempotentOrder }
            .run(idempotentOrderOperationDao::save)
    }

    override fun saveOperation(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation =
        idempotentOrderOperationDao.save(idempotentOrderOperation)

    private fun findIdempotentOrderOrCreateNewOne(operationRequest: OperationRequest): IdempotentOrder =
        idempotentOrderDao.findIdempotentOrderByOrderId(operationRequest.orderId)
            ?: saveNewOrder(operationRequest)

    private fun saveNewOrder(operationRequest: OperationRequest): IdempotentOrder =
        operationRequest
            .run(idempotentOrderOperationMapper::toIdempotentOrder)
            .run(idempotentOrderDao::save)
}
