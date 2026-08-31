package ru.sogaz.site.paymentService.service.v2.status.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.dto.data.SbpGpbStateCallbackRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbCallbackMapper
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbGidCallbackRequest
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
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
    private val log = loggerFor(javaClass)

    override fun updateByGpbGidCallback(request: GpbGidCallbackRequest) {
        val merchantTrx = request.result.merchantTrx?.let(UUID::fromString)
        val orderOperation =
            idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(merchantTrx, request.pgaTrxId)
                ?: throw OperationNotFoundException(request.pgaTrxId)

        checkOperationStatusProducer.sendCheckStatusEvent(orderOperation)
    }

    override fun updateByGpbCardCallback(gpbCardCallback: GpbCardCallback) {
        val totalStart = System.nanoTime()

        log.info(
            "GPB callback start: trxId={}, merchantTrx={}, resultCode={}, extResultCode={}",
            gpbCardCallback.trx_id,
            gpbCardCallback.merchant_trx,
            gpbCardCallback.result_code,
            gpbCardCallback.extResultCode,
        )

        val findStart = System.nanoTime()

        val orderOperation =
            findOrderOperationOrThrow(UUID.fromString(gpbCardCallback.merchant_trx), gpbCardCallback.trx_id)

        log.info(
            "GPB callback step=findOrderOperation tookMs={}, operationId={}, currentState={}",
            elapsedMs(findStart),
            orderOperation.id,
            orderOperation.state,
        )

        if (orderOperation.state.isFinaleState()) {
            log.info(
                "GPB callback skipped: operation already final, operationId={}, state={}, totalMs={}",
                orderOperation.id,
                orderOperation.state,
                elapsedMs(totalStart),
            )
            return
        }

        val mapStart = System.nanoTime()

        val operationDetails = gpbCallbackMapper.toBankOperationDetails(gpbCardCallback)

        log.info(
            "GPB callback step=mapCallback tookMs={}, operationId={}, mappedState={}, errorText={}, bankId={}",
            elapsedMs(mapStart),
            orderOperation.id,
            operationDetails.state,
            operationDetails.errorText,
            operationDetails.bankId,
        )

        val isFinalStart = System.nanoTime()
        val isFinalState = operationDetails.state.isFinaleState()

        log.info(
            "GPB callback step=checkFinalState tookMs={}, operationId={}, mappedState={}, isFinal={}",
            elapsedMs(isFinalStart),
            orderOperation.id,
            operationDetails.state,
            isFinalState,
        )

        if (isFinalState) {
            val updateStart = System.nanoTime()

            operationStatusUpdater.updateByOperationDetails(orderOperation, operationDetails)

            log.info(
                "GPB callback step=updateByOperationDetails tookMs={}, operationId={}, mappedState={}",
                elapsedMs(updateStart),
                orderOperation.id,
                operationDetails.state,
            )
        } else {
            log.info(
                "GPB callback not updated: mapped state is not final, operationId={}, mappedState={}",
                orderOperation.id,
                operationDetails.state,
            )
        }

        log.info(
            "GPB callback finish: trxId={}, merchantTrx={}, resultCode={}, mappedState={}, totalMs={}",
            gpbCardCallback.trx_id,
            gpbCardCallback.merchant_trx,
            gpbCardCallback.result_code,
            operationDetails.state,
            elapsedMs(totalStart),
        )
    }

    private fun elapsedMs(start: Long): Long =
        (System.nanoTime() - start) / 1_000_000
    fun findOrderOperationOrThrow(
        orderId: UUID,
        paymentBankId: String,
    ) = idempotentOrderOperationDao.findByOrderIdAndPaymentBankId(orderId, paymentBankId)
        ?: throw OperationNotFoundException(paymentBankId)

    fun findOrderOperationSbpOrThrow(qrId: String) =
        idempotentOrderOperationDao.findByQrId(qrId)
            ?: throw OperationNotFoundException(qrId)

    override fun updateByQrId(request: SbpGpbStateCallbackRequest) {
        val orderOperation = findOrderOperationSbpOrThrow(request.qrcId)
        orderOperation.paymentBankId = request.transactionId
        idempotentOrderOperationDao.save(orderOperation)
        checkOperationStatusProducer.sendCheckStatusEvent(orderOperation)
    }

    override fun processSbpReversalCallback(paymentBankId: String) {
        val reversalOperation =
            idempotentOrderOperationDao.findFirstByPaymentBankIdAndOperationType(paymentBankId, OperationType.REVERSAL)
                ?: return

        if (reversalOperation.state.isFinaleState() || reversalOperation.state == OperationState.CALLBACK) {
            return
        }

        reversalOperation.state = OperationState.CALLBACK
        idempotentOrderOperationDao.save(reversalOperation)
        checkOperationStatusProducer.sendCheckStatusEvent(reversalOperation)
    }
}
