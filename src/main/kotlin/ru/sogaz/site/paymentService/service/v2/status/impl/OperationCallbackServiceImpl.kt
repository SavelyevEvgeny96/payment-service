package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCallbackMapper
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.exception.OperationNotFoundException
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.status.OperationCallbackService
import ru.sogaz.site.paymentService.service.v2.status.OperationStatusUpdater
import java.util.UUID

@Service
@Transactional(rollbackFor = [Exception::class])
class OperationCallbackServiceImpl(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val checkOperationStatusProducer: CheckOperationStatusProducer,
    private val gpbCallbackMapper: GpbCallbackMapper,
    private val operationStatusUpdater: OperationStatusUpdater,
) : OperationCallbackService {
    override fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback) {
        val orderOperation = findOrderOperationOrThrow(UUID.fromString(gpbCardCallback.merchant_trx), gpbCardCallback.trx_id)
        if (orderOperation.state.isFinaleState()) {
            return
        }
        val operationDetails = gpbCallbackMapper.toBankOperationDetails(gpbCardCallback)
        if (operationDetails.state.isFinaleState()) {
            operationStatusUpdater.updateByOperationDetails(orderOperation, operationDetails)
        }
    }

    fun findOrderOperationOrThrow(
        orderId: UUID,
        paymentBankId: String,
    ) = idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(orderId, paymentBankId)
        ?: throw OperationNotFoundException(paymentBankId)

    fun findOrderOperationSbpOrThrow(qrId: String) =
        idempotentOrderOperationDao.findByQrId(qrId)
            ?: throw OperationNotFoundException(qrId)

    override fun updateByQrId(qrId: String) {
        val orderOperation = findOrderOperationSbpOrThrow(qrId)
        checkOperationStatusProducer.sendCheckStatusEvent(orderOperation)
    }
}
