package ru.sogaz.site.paymentService.service.callback

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentExtendedCodeMessage
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.OrderPaidEventFactory
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.metrics.GpbCallbackMetricServiceImpl
import ru.sogaz.site.paymentService.service.rabbit.OrderPaidEventProducer

@Service
class GpbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val signatureVerifier: SignatureVerifier,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val gpbCallbackMetricService: GpbCallbackMetricServiceImpl,
    private val waitingPaymentDao: WaitingPaymentDao,
    private val orderPaidEventProducer: OrderPaidEventProducer,
    private val orderPaidEventFactory: OrderPaidEventFactory,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val INVALID_CODE = "Invalid resultCode"
        const val NOT_FOUND = "Not Found"
        const val CONST_CALLBACK = "CALLBACK"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_TRX_ID = "Произошла ошибка сертификата для trx_id: "
        const val START_METHOD_PROCESS_CALL =
            ">>> СТАРТ метода проверки CALLBACK от банка" +
                " traceID: "

        const val UPDATE_PAYMENT_STATUS = "Статус платежа в таблице ПЛАТЕЖЕЙ обновлен. paymentBankId: "
        const val OPERATION_PAYMENT_SUCCESS = "Запись в таблицу истории операций добавлена. paymentBankId: "
        const val ERROR_SAVE_OPERATIONS = "Ошибка сохранения истории операций в таблицу"
        const val CALLBACK_TABLE_SAVE_SUCCESS = "Запись в таблицу CALLBACK добавлена. paymentBankId: "
        const val CALLBACK_RESULT_CODE_PROCESS_SUCCESS = "Payment и order успешно обновлены, orderId = {}"
        const val CALLBACK_RESULT_CODE_PROCESS_FAIL = "Не удалось обработать payment, orderId = {}, error= {}"
        const val RESULT_CODE_SUCCESS = 1
        const val RESULT_CODE_FAIL = 2
    }

    override fun processCallback(
        requestDto: GpbCallbackRequest,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String> {
        return try {
            val traceId = getTraceId()
            logger.debug(START_METHOD_PROCESS_CALL + traceId)

            val trxId = requestDto.trxId

            if (!signatureVerifier.verifySignature(requestDto, httpServletRequest)) {
                logger.debug(ERROR_TRX_ID + trxId)
                return createErrorResponse(INVALID_SIGNATURE)
            }

            gpbCallbackMetricService.setMetric(requestDto)

            val payment =
                paymentDao.findByPaymentBankId(requestDto.trxId)

            val order =
                payment.order.id
                    ?.let(orderDao::findById)
                    ?: return createErrorResponse(NOT_FOUND)

            when (requestDto.resultCode) {
                RESULT_CODE_SUCCESS -> {
                    waitingPaymentDao.deleteByPaymentBankId(trxId)
                    processSuccess(payment, order)
                }

                RESULT_CODE_FAIL -> {
                    waitingPaymentDao.deleteByPaymentBankId(trxId)
                    processFail(payment, order, requestDto)
                }

                else -> {
                    logger.warn("Unknown resultCode=${requestDto.resultCode}")
                    return createErrorResponse(INVALID_CODE)
                }
            }

            logOperation(payment)
            logger.debug(OPERATION_PAYMENT_SUCCESS)

            saveCallbackPayment(payment)
            logger.debug("$CALLBACK_TABLE_SAVE_SUCCESS ${payment.paymentBankId}")

            createSuccessResponse()
        } catch (e: InnerException) {
            logger.error(ERROR_TRX_ID + requestDto.trxId)
            createErrorResponse(NOT_FOUND)
        } catch (e: Exception) {
            logger.error(ERROR_TRX_ID + requestDto.trxId)
            createErrorResponse(INTERNAL_SERVER_ERROR)
        }
    }

    private fun processSuccess(
        payment: Payment,
        order: Order,
    ) {
        updatePaymentStatus(payment, PaymentStatusEnum.SUCCESS)
        updateOrderStatus(order, OrderStatus.SUCCESS)

        orderPaidEventProducer.send(
            orderPaidEventFactory.success(
                orderId = order.id,
            ),
        )

        logger.debug(CALLBACK_RESULT_CODE_PROCESS_SUCCESS, order.id)
    }

    private fun processFail(
        payment: Payment,
        order: Order,
        requestDto: GpbCallbackRequest,
    ) {
        updatePaymentStatus(payment, PaymentStatusEnum.FAIL)

        orderPaidEventProducer.send(
            orderPaidEventFactory.error(
                orderId = order.id,
                errorText = buildErrorText(requestDto.extResultCode),
            ),
        )

        logger.debug(CALLBACK_RESULT_CODE_PROCESS_FAIL, order.id, requestDto.extResultCode)
    }

    private fun updatePaymentStatus(
        payment: Payment,
        status: PaymentStatusEnum,
    ) {
        payment
            .apply { state = status }
            .run { paymentDao.save(this) }
    }

    private fun updateOrderStatus(
        order: Order,
        state: OrderStatus,
    ) {
        order
            .apply { status = state }
            .run { orderDao.save(this) }
    }

    private fun buildErrorText(extResultCode: String?): String =
        extResultCode?.let { code ->
            "$code. ${PaymentExtendedCodeMessage.fromCode(code)}"
        } ?: "UNKNOWN_ERROR"

    private fun saveCallbackPayment(payment: Payment) = callbackPaymentDao.saveCallbackForPayment(payment)

    private fun logOperation(payment: Payment) {
        try {
            paymentOperationHistoryDao.saveRecordOperationHistory(
                payment.order,
                getTraceId(),
                ActionType.CALLBACK_RECEIVED.value,
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
