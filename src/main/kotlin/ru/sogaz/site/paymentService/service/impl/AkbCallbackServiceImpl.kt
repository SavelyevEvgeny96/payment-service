package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.dto.AkbCallbackResponse
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.AkbCallbackService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime

class AkbCallbackServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val getPaymentDao: GetPaymentDao,
    private val orderDao: OrderDao,
    private val callbackPaymentStatus: PaymentStatus,
    private val callbackAction: ActionType,
    private val payClientSystem: ClientSystem,
) : AkbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_BANK_ID = "Произошла ошибка для bank_id: "
        const val CODE_SUCCESS = 1101511200
        const val STATUS_OK = "OK"
        const val ERROR_PAYMENT_UPDATE = "Произошла ошибка при обновлении платежа в БД"
    }

    override fun processCallback(request: AkbCallbackRequest): Response<AkbCallbackResponse> {
        val traceId = getTraceId()
        return try {
            val payment = getPaymentDao.getPaymentFromBankId(request.bankId)

            updatePaymentStatus(payment, traceId)

            logOperation(payment, traceId)
            val response = AkbCallbackResponse(STATUS_OK)

            getSuccessResponse(traceId, CODE_SUCCESS, response)
        } catch (e: Exception) {
            logger.info(ERROR_BANK_ID + request.bankId, e)
            throw InnerException(traceId, ERROR_BANK_ID + request.bankId)
        }
    }

    private fun updatePaymentStatus(
        payment: Payment,
        traceId: String,
    ) {
        try {
            payment.stateId = callbackPaymentStatus
            payment.updateDate = LocalDateTime.now()
            paymentRepository.save(payment)
        } catch (e: Exception) {
            logger.info(ERROR_PAYMENT_UPDATE)
            throw InnerException(traceId, e.message)
        }
    }

    private fun logOperation(
        payment: Payment,
        traceId: String,
    ) {
        try {
            val orderId =
                payment.orderId ?: throw InnerException(
                    getTraceId(),
                    ORDER_NOT_FOUND,
                )
            val order = orderId.orderId?.let { orderDao.getOrderId(traceId, it) }

            operationHistoryRepository.save(
                PaymentOperationHistory(
                    action = callbackAction,
                    actionDate = LocalDateTime.now(),
                    actionAuthor = payClientSystem,
                    order = order,
                ),
            )
        } catch (e: Exception) {
            throw e
        }
    }
}
