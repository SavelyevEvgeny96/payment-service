package ru.sogaz.site.paymentService.service.callback

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.dto.response.CallbackResponse
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.CallbackService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime

class CallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
) : CallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_BANK_ID =
            "Ошибка запроса смены статуса. Указанный ордер операции по банковской карте не найден"
        const val CODE_SUCCESS = 1101511200
        const val STATUS_OK = "OK"
        const val ERROR_PAYMENT_UPDATE = "Произошла ошибка при обновлении платежа в БД"
        const val AKB_RUS = "akb_rus"
        const val SBP_GPB = "gpb"
        const val BANK_CARD = "bankCard"
        const val START_METHOD_PROCESS_CALL =
            ">>> СТАРТ метода проверки CALLBACK" +
                " traceID: "
        const val UPDATE_PAYMENT_STATUS = "Статус платежа в таблице ПЛАТЕЖЕЙ обновлен. paymentBankId: "
        const val OPERATION_PAYMENT_SUCCESS = "Запись в таблицу истории операций добавлена. paymentBankId: "
        const val OPERATION_PAYMENT_FAIL = "Запись в таблицу истории операций не добавлена. Произошла ошибка"
        const val CALLBACK_TABLE_SAVE_SUCCESS = "Запись в таблицу CALLBACK добавлена. paymentBankId: "
    }

    override fun processCallback(request: CallbackRequest): Response<CallbackResponse> {
        val traceId = getTraceId()
        logger.info("$START_METHOD_PROCESS_CALL $traceId")
        return try {
            val payment = paymentDao.getPaymentFromBankId(request.bankId)

            updatePaymentStatus(payment, traceId)
            logger.info("$UPDATE_PAYMENT_STATUS ${payment.paymentBankId}")

            logOperation(payment, traceId)
            logger.info("$OPERATION_PAYMENT_SUCCESS ${payment.paymentBankId}")

            saveCallbackPayment(payment)
            logger.info("$CALLBACK_TABLE_SAVE_SUCCESS ${payment.paymentBankId}")

            val response = CallbackResponse(STATUS_OK)

            getSuccessResponse(traceId, CODE_SUCCESS, response)
        } catch (e: Exception) {
            logger.error(ERROR_BANK_ID + request.bankId)
            throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_AKB, traceId)
        }
    }

    private fun saveCallbackPayment(payment: Payment) {
        val existingPayment = payment.paymentBankId?.let { callbackPaymentDao.findByPaymentBankId(it) }

        if (existingPayment == null) {
            val newCallbackPayment =
                CallbackPayment(
                    bankId =
                        when (payment.bank) {
                            BankEnum.AKB_RUS -> AKB_RUS
                            else -> SBP_GPB
                        },
                    typeId = BANK_CARD,
                    paymentBankId = payment.paymentBankId,
                    createDate = LocalDateTime.now(),
                    updateDate = LocalDateTime.now(),
                )
            callbackPaymentDao.save(newCallbackPayment)
        } else {
            existingPayment.apply {
                bankId =
                    when (payment.bank) {
                        BankEnum.AKB_RUS -> AKB_RUS
                        else -> SBP_GPB
                    }
                typeId = BANK_CARD
                paymentBankId = payment.paymentBankId
                updateDate = LocalDateTime.now()
            }
            callbackPaymentDao.save(existingPayment)
        }
    }

    private fun updatePaymentStatus(
        payment: Payment,
        traceId: String,
    ) {
        try {
            payment.state = PaymentStatusEnum.CALLBACK
            payment.updateDate = LocalDateTime.now()
            paymentDao.save(payment)
        } catch (e: Exception) {
            logger.error(ERROR_PAYMENT_UPDATE)
            throw InnerException(traceId, e.message)
        }
    }

    private fun logOperation(
        payment: Payment,
        traceId: String,
    ) {
        try {
            val orderId =
                payment.order ?: throw InnerException(
                    traceId,
                    ORDER_NOT_FOUND,
                )
            val order = orderDao.findById(orderId.id!!).run { this ?: throw InnerException(traceId, ORDER_NOT_FOUND) }
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                traceId,
                ActionType.CALLBACK_RECEIVED.value,
            )
        } catch (e: Exception) {
            logger.error(OPERATION_PAYMENT_FAIL)
            throw e
        }
    }
}
