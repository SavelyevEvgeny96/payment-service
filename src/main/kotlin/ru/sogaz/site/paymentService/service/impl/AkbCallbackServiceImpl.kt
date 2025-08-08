package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.dto.AkbCallbackResponse
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.AkbCallbackService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime

class AkbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val callbackPaymentStatus: PaymentStatus,
    private val callbackAction: ActionType,
    private val payClientSystem: ClientSystem,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
) : AkbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_BANK_ID = "Ошибка запроса смены статуса. Указанный ордер операции по банковской карте АКБ Россия не найден"
        const val CODE_SUCCESS = 1101511200
        const val STATUS_OK = "OK"
        const val ERROR_PAYMENT_UPDATE = "Произошла ошибка при обновлении платежа в БД"
        const val AKB_RUS = "akb_rus"
        const val BANK_CARD = "bankCard"
    }

    override fun processCallback(request: AkbCallbackRequest): Response<AkbCallbackResponse> {
        val traceId = getTraceId()
        return try {
            val payment = paymentDao.getPaymentFromBankId(request.bankId)

            updatePaymentStatus(payment, traceId)

            logOperation(payment, traceId)

            saveCallbackPayment(payment)

            val response = AkbCallbackResponse(STATUS_OK)

            getSuccessResponse(traceId, CODE_SUCCESS, response)
        } catch (e: Exception) {
            logger.info(ERROR_BANK_ID + request.bankId, e)
            throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_AKB, traceId)
        }
    }

    private fun saveCallbackPayment(payment: Payment) {
        val callbackPayment =
            CallbackPayment(
                bankId = AKB_RUS,
                typeId = BANK_CARD,
                paymentBankId = payment.paymentBankId,
                createDate = LocalDateTime.now(),
                updateDate = LocalDateTime.now(),
            )
        callbackPaymentDao.save(callbackPayment)
    }

    private fun updatePaymentStatus(
        payment: Payment,
        traceId: String,
    ) {
        try {
            payment.stateId = callbackPaymentStatus
            payment.updateDate = LocalDateTime.now()
            paymentDao.save(payment)
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

            paymentOperationHistoryDao.save(
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
