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
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.AkbCallbackService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime

class AkbCallbackServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusRepository: PaymentStatusRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val paymentStatusService: PaymentStatusCheckerService,
    private val getPaymentDao: GetPaymentDao,
    private val orderDao: OrderDao,
) : AkbCallbackService {
    private val logger = loggerFor(javaClass)

    private val callbackPaymentStatus: PaymentStatus by lazy {
        paymentStatusRepository.findByStateId(CONST_CALLBACK)
            ?: throw IllegalStateException("Cтатус платежа CALLBACK_AKB не найден")
    }

    private val callbackAction: ActionType by lazy {
        actionTypeRepository.findByActionName(CALLBACK_SUCCESS)
            ?: throw IllegalStateException("ActionType для CALLBACK_SUCCESS не найден")
    }

    private val payClientSystem: ClientSystem by lazy {
        clientSystemRepository.findByExternalSystemCode(PAY)
            ?: throw IllegalStateException("Автор для PAY не найден")
    }

    companion object {
        const val CONST_CALLBACK = "CALLBACK_AKB"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val CALLBACK_SUCCESS = "Получение CALLBACK от АКБ Россия"
        const val PAY = "PAY"
        const val ERROR_BANK_ID = "Произошла ошибка для bank_id: "
        const val CODE_SUCCESS = 1101511200
        const val STATUS_OK = "OK"
    }

    override fun processCallback(request: AkbCallbackRequest): Response<AkbCallbackResponse> {
        val traceId = getTraceId()
        return try {
            val payment = getPaymentDao.getPaymentFromBankId(request.bankId, traceId)

            if (payment != null) {
                updatePaymentStatus(payment)

                logOperation(payment, traceId)

            }
            val response = AkbCallbackResponse(STATUS_OK)

            getSuccessResponse(traceId, CODE_SUCCESS, response)
        } catch (e: Exception) {
            logger.info(ERROR_BANK_ID + request.bankId, e)
            throw InnerException(traceId, ERROR_BANK_ID + request.bankId)
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        payment.stateId = callbackPaymentStatus
        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
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
