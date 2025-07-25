package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.ActionType
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import java.time.LocalDateTime

class GpbCallbackServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val paymentStatusService: PaymentStatusCheckerService,
    private val signatureVerifier: SignatureVerifier
): GpbCallbackService {

    private val logger = loggerFor(javaClass)

    override fun processCallback(request: GpbCallbackRequest): ResponseEntity<String> {
        return try {
            if (!signatureVerifier.verifySignature(request.rawQueryString, request.signature)) {
                logger.info("Произошла ошибка для trx_id: ${request.trxId}")
                return createErrorResponse("Invalid signature")
            }

            val payment = paymentRepository.findByPaymentBankId(request.trxId)
                ?: return createErrorResponse("Платеж не найден")

            updatePaymentStatus(payment)

            logOperation(payment)

            payment.paymentBankId?.let { paymentStatusService.getStatus(it, getTraceId()) }

            createSuccessResponse()
        } catch (e: Exception) {
            logger.info("Произошла ошибка для trx_id: ${request.trxId}", e)
            createErrorResponse("Internal server error")
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        val paymentStatus = PaymentStatus()
        paymentStatus.stateId = "CALLBACK"
        payment.stateId = paymentStatus
        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
    }

    private fun logOperation(payment: Payment) {
        val order = orderRepository.findByOrderId(payment.orderId?.orderId.toString())

        val actions = ActionType()
        actions.actionName = "Получение CALLBACK от ГПБ"
        val actionsAuthor = ClientSystem()
        actionsAuthor.externalSystemName = "Сервис оплат"
        operationHistoryRepository.save(
            PaymentOperationHistory(
                action = actions,
                actionDate = LocalDateTime.now(),
                actionAuthor = actionsAuthor,
                order = order.get()
            )
        )
    }

    private fun createSuccessResponse(): ResponseEntity<String> {
        val response = """
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
        val response = """
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