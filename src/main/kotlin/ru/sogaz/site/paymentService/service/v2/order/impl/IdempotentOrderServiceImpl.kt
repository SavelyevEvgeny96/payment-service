package ru.sogaz.site.paymentService.service.v2.order.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderMapper
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService

@Service
class IdempotentOrderServiceImpl(
    private val idempotentOrderDao: IdempotentOrderDao,
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val idempotentOrderMapper: IdempotentOrderMapper,
) : IdempotentOrderService {
    override fun saveOperation(payOperationRequest: PayOperationRequest): IdempotentOrderOperation {
        val idempotentOrder = findIdempotentOrderOrCreateNewOne(payOperationRequest)
        val idempotentOrderOperation =
            idempotentOrderMapper.toIdempotentOrderOperation(idempotentOrder, payOperationRequest)
        return idempotentOrderOperationDao.save(idempotentOrderOperation)
    }

    override fun updateOperation(
        idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation =
        idempotentOrderOperation
            .run { idempotentOrderMapper.updateIdempotentOrderOperation(this, bankPaymentPageData) }
            .run(idempotentOrderOperationDao::save)

    private fun findIdempotentOrderOrCreateNewOne(operationRequest: OperationRequest): IdempotentOrder =
        idempotentOrderDao.findIdempotentOrderByOrderId(operationRequest.orderId)
            ?: saveNewOrder(operationRequest)

    private fun saveNewOrder(operationRequest: OperationRequest): IdempotentOrder =
        operationRequest
            .run(idempotentOrderMapper::toIdempotentOrder)
            .run(idempotentOrderDao::save)
}
