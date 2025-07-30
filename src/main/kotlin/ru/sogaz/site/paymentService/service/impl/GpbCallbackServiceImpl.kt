package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.time.LocalDateTime

class GpbCallbackServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val paymentStatusService: PaymentStatusCheckerService,
    private val signatureVerifier: SignatureVerifier,
    private val paymentStatusRepository: PaymentStatusRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val clientSystemRepository: ClientSystemRepository,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_NOT_FOUND = "Платеж не найден"
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val CONST_CALLBACK = "CALLBACK"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val CALLBACK_SUCCESS = "Получение CALLBACK от ГПБ"
        const val PAY = "PAY"
        const val ERROR_TRX_ID = "Произошла ошибка для trx_id: "
    }

    override fun processCallback(request: GpbCallbackRequest): ResponseEntity<String> {
        return try {
            if (!signatureVerifier.verifySignature(request.signature)) {
                logger.info(ERROR_TRX_ID + request.trxId)
                return createErrorResponse(INVALID_SIGNATURE)
            }

            val payment =
                paymentRepository.findByPaymentBankId(request.trxId)
                    ?: return createErrorResponse(PAYMENT_NOT_FOUND)

            updatePaymentStatus(payment)

            logOperation(payment)

            payment.paymentBankId?.let { paymentStatusService.getStatus(it, getTraceId()) }

            createSuccessResponse()
        } catch (e: Exception) {
            logger.info(ERROR_TRX_ID + request.trxId, e)
            createErrorResponse(INTERNAL_SERVER_ERROR)
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
            val orderId = payment.orderId?.id ?: throw InnerException(getTraceId(), ORDER_NOT_FOUND)
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
