package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.time.LocalDateTime

class GpbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val signatureVerifier: SignatureVerifier,
    private val paymentStatusDao: PaymentStatusDao,
    private val getOrderStatusDao: OrderStatusDao,
    private val callbackAction: ActionType,
    private val payClientSystem: ClientSystem,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val NOT_FOUND = "Not Found"
        const val CONST_CALLBACK = "SUCCESS"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_TRX_ID = "Произошла ошибка сертификата для trx_id: "
        const val START_METHOD_PROCESS_CALL =
            ">>> СТАРТ метода проверки CALLBACK от банка" +
                    " traceID: "
        const val UPDATE_PAYMENT_STATUS = "Статус платежа в таблице ПЛАТЕЖЕЙ обновлен. paymentBankId: "
        const val UPDATE_ORDER_STATUS = "Статус заказа в таблице ЗАКАЗОВ обновлен. paymentBankId: "
        const val OPERATION_PAYMENT_SUCCESS = "Запись в таблицу истории операций добавлена. paymentBankId: "
        const val ERROR_SAVE_OPERATIONS = "Ошибка сохранения истории операций в таблицу"
    }

    override fun processCallback(request: GpbCallbackRequest): ResponseEntity<String> {
        return try {
            val traceId = getTraceId()
            if (!signatureVerifier.verifySignature(request.signature)) {
                logger.info(ERROR_TRX_ID + request.trxId)
                return createErrorResponse(INVALID_SIGNATURE)
            }

            val payment =
                paymentDao.findByPaymentBankId(request.trxId)

            if (payment.orderId == null ||
                payment.orderId?.orderId?.let {
                    orderDao.getOrderId(it)
                } == null
            ) {
                return createErrorResponse(NOT_FOUND)
            }

            updatePaymentStatus(payment)

            updateOrderStatus(order = payment.orderId!!)

            logOperation(payment)

            createSuccessResponse()
        } catch (e: InnerException) {
            logger.error(ERROR_TRX_ID + request.trxId)
            createErrorResponse(NOT_FOUND)
        } catch (e: Exception) {
            logger.error(ERROR_TRX_ID + request.trxId)
            createErrorResponse(INTERNAL_SERVER_ERROR)
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        val traceId = getTraceId()
        val paymentStatus = paymentStatusDao.getPaymentStatus(traceId, CONST_CALLBACK)
        payment.stateId = paymentStatus
        payment.updateDate = LocalDateTime.now()
        paymentDao.save(payment)
    }

    private fun updateOrderStatus(order: Order) {
        val traceId = getTraceId()
        val orderStatus = getOrderStatusDao.getOrderStatus(traceId, CONST_CALLBACK)
        order.orderStatus = orderStatus
        order.updateDate = LocalDateTime.now()
        orderDao.save(order)
    }

    private fun logOperation(payment: Payment) {
        try {
            val traceId = getTraceId()
            val orderId = payment.orderId ?: throw InnerException(getTraceId(), ORDER_NOT_FOUND)
            val order =
                orderId.orderId?.let {
                    orderDao.getOrderId(it)
                }
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                payClientSystem,
                traceId,
                callbackAction.actionName
            )
        } catch (e: Exception) {
            logger.error(ERROR_SAVE_OPERATIONS + e.message)
            throw e
        }
    }

    private fun createSuccessResponse(): ResponseEntity<String> {
        val response =
            """
            <register-payment-response>
              <result>
                <code>1</code>
                <desc>OK</desc>
              </result>
            </register-payment-response>
            """.trimIndent()
        return ResponseEntity.ok(response)
    }

    private fun createErrorResponse(description: String): ResponseEntity<String> {
        val response =
            """
            <register-payment-response>
              <result>
                <code>2</code>
                <desc>$description</desc>
              </result>
            </register-payment-response>
            """.trimIndent()
        return ResponseEntity.ok(response)
    }
}
