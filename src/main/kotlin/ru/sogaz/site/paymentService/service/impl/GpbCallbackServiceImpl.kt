package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.time.LocalDateTime

class GpbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val signatureVerifier: SignatureVerifier,
    private val getPaymentStatusDao: GetPaymentStatusDao,
    private val getOrderStatusDao: OrderStatusDao,
    private val callbackAction: ActionType,
    private val payClientSystem: ClientSystem,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val CONST_CALLBACK = "SUCCESS"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_TRX_ID = "Произошла ошибка для trx_id: "
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
                    orderDao.getOrderId(traceId, it)
                } == null
            ) {
                return createErrorResponse(INTERNAL_SERVER_ERROR)
            }

            updatePaymentStatus(payment)

            updateOrderStatus(order = payment.orderId!!)

            logOperation(payment)

            createSuccessResponse()
        } catch (e: Exception) {
            logger.info(ERROR_TRX_ID + request.trxId, e)
            createErrorResponse(INTERNAL_SERVER_ERROR)
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        val traceId = getTraceId()
        val paymentStatus = getPaymentStatusDao.getPaymentStatus(traceId, CONST_CALLBACK)
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
        val traceId = getTraceId()
        try {
            val orderId = payment.orderId ?: throw InnerException(getTraceId(), ORDER_NOT_FOUND)
            val order =
                orderId.orderId?.let {
                    orderDao.getOrderId(traceId, it)
                }

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
