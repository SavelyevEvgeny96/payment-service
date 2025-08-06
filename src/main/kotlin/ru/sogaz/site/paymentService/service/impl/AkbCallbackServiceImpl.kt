package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.AkbCallbackRequest
import ru.sogaz.site.paymentService.dto.AkbCallbackResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
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
    private val orderRepository: OrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val paymentStatusService: PaymentStatusCheckerService,
) : AkbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val CONST_CALLBACK = "CALLBACK"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val CALLBACK_SUCCESS = "Получение CALLBACK от АКБ Россия"
        const val PAY = "PAY"
        const val ERROR_BANK_ID = "Произошла ошибка для bank_id: "
        const val CODE_SUCCESS = 1101511200
    }

    override fun processCallback(request: AkbCallbackRequest): Response<AkbCallbackResponse> {
        val traceId = getTraceId()
        return try {
            val payment =
                paymentRepository.findByPaymentBankId(request.bankId)
                    ?: throw InnerException(traceId, "")

            updatePaymentStatus(payment)

            logOperation(payment)

            payment.paymentBankId?.let { paymentStatusService.getStatus(it, getTraceId()) }

            val response = AkbCallbackResponse("OK")

            getSuccessResponse(traceId, CODE_SUCCESS, response)
        } catch (e: Exception) {
            logger.info(ERROR_BANK_ID + request.bankId, e)
            throw InnerException(traceId, "")
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        val paymentStatus = paymentStatusRepository.findByStateId(CONST_CALLBACK)
        payment.stateId = paymentStatus
        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
    }

    private fun logOperation(payment: Payment) {
        try {
            val orderId =
                payment.orderId?.id ?: throw InnerException(
                    getTraceId(),
                    ORDER_NOT_FOUND,
                )
            val order =
                orderRepository.findById(orderId).orElseThrow {
                    InnerException(getTraceId(), ORDER_NOT_FOUND + orderId)
                }

            val actions = actionTypeRepository.findByActionName(CALLBACK_SUCCESS)
            val actionsAuthor = clientSystemRepository.findByExternalSystemCode(PAY)
            operationHistoryRepository.save(
                PaymentOperationHistory(
                    action = actions,
                    actionDate = LocalDateTime.now(),
                    actionAuthor = actionsAuthor,
                    order = order,
                ),
            )
        } catch (e: Exception) {
            throw e
        }
    }
}
