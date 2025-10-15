package ru.sogaz.site.paymentService.service.callback

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import java.time.LocalDateTime

class GpbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val orderDao: OrderDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val signatureVerifier: SignatureVerifier,
    private val apiConfigProperties: ApiConfigProperties,
    private val callbackPaymentDao: CallbackPaymentDao,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val NOT_FOUND = "Not Found"
        const val CONST_CALLBACK = "CALLBACK"
        const val ORDER_NOT_FOUND = "Order ID не найден"
        const val ERROR_TRX_ID = "Произошла ошибка сертификата для trx_id: "
        const val START_METHOD_PROCESS_CALL =
            ">>> СТАРТ метода проверки CALLBACK от банка" +
                " traceID: "
        const val UPDATE_PAYMENT_STATUS = "Статус платежа в таблице ПЛАТЕЖЕЙ обновлен. paymentBankId: "
        const val UPDATE_ORDER_STATUS = "Статус заказа в таблице ЗАКАЗОВ обновлен. paymentBankId: "
        const val OPERATION_PAYMENT_SUCCESS = "Запись в таблицу истории операций добавлена. paymentBankId: "
        const val ERROR_SAVE_OPERATIONS = "Ошибка сохранения истории операций в таблицу"
        const val CALLBACK_TABLE_SAVE_SUCCESS = "Запись в таблицу CALLBACK добавлена. paymentBankId: "
    }

    override fun processCallback(request: GpbCallbackRequest): ResponseEntity<String> {
        return try {
            val traceId = getTraceId()
            logger.info(START_METHOD_PROCESS_CALL + traceId)

            if (!signatureVerifier.verifySignature(request)) {
                logger.info(ERROR_TRX_ID + request.trxId)
                return createErrorResponse(INVALID_SIGNATURE)
            }

            val payment =
                paymentDao.findByPaymentBankId(request.trxId)

            if (payment.order == null ||
                payment.order
                    ?.id
                    ?.let {
                        orderDao.findById(it)
                    }?.isEmpty == true
            ) {
                return createErrorResponse(NOT_FOUND)
            }

            updatePaymentStatus(payment)
            logger.info(UPDATE_PAYMENT_STATUS)

            updateOrderStatus(order = payment.order!!)
            logger.info(UPDATE_ORDER_STATUS)

            logOperation(payment)
            logger.info(OPERATION_PAYMENT_SUCCESS)

            saveCallbackPayment(payment)
            logger.info("$CALLBACK_TABLE_SAVE_SUCCESS ${payment.paymentBankId}")

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
        payment
            .apply { state = PaymentStatusEnum.SUCCESS }
            .run { paymentDao.save(this) }
    }

    private fun updateOrderStatus(order: Order) {
        order
            .apply { status = OrderStatus.SUCCESS }
            .run { orderDao.save(this) }
    }

    private fun saveCallbackPayment(payment: Payment) {
        val existingPayment = payment.paymentBankId?.let { callbackPaymentDao.findByPaymentBankId(it) }

        if (existingPayment == null) {
            val newCallbackPayment =
                CallbackPayment(
                    bankId =
                        when (payment.bank) {
                            BankEnum.GPB -> CallbackServiceImpl.SBP_GPB
                            else -> CallbackServiceImpl.AKB_RUS
                        },
                    typeId = CallbackServiceImpl.BANK_CARD,
                    paymentBankId = payment.paymentBankId,
                    createDate = LocalDateTime.now(),
                    updateDate = LocalDateTime.now(),
                )
            callbackPaymentDao.save(newCallbackPayment)
        } else {
            existingPayment.apply {
                bankId =
                    when (payment.bank) {
                        BankEnum.GPB -> CallbackServiceImpl.SBP_GPB
                        else -> CallbackServiceImpl.AKB_RUS
                    }
                typeId = CallbackServiceImpl.BANK_CARD
                paymentBankId = payment.paymentBankId
                updateDate = LocalDateTime.now()
            }
            callbackPaymentDao.save(existingPayment)
        }
    }

    private fun logOperation(payment: Payment) {
        try {
            val traceId = getTraceId()
            val orderId = payment.order ?: throw InnerException(getTraceId(), ORDER_NOT_FOUND)
            val order =
                orderId.id?.let {
                    orderDao.findById(it).get()
                }
            paymentOperationHistoryDao.saveRecordOperationHistory(
                order,
                traceId,
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
