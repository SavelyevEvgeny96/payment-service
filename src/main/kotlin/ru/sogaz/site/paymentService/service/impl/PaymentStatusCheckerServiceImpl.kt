package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpMethod
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS_BANK
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.data.QueueMessageDto
import ru.sogaz.site.paymentService.dto.data.VariableDto
import ru.sogaz.site.paymentService.dto.response.PaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.PAYMENT_PREFIX
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_SUCCESS
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class PaymentStatusCheckerServiceImpl(
    private val paymentDao: PaymentDao,
    private val restTemplate: WebConfigRestTemplate,
    private val apiConfigProperty: ApiConfigProperties,
    private val receiptService: ReceiptService,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val rabbit: RabbitProperties,
    private val subOrderDao: SubOrderDao,
    private val paymentStatusDao: PaymentStatusDao,
    private val orderStatusDao: OrderStatusDao,
    private val orderRepository: OrderRepository,
    private val operationHistoryDao: PaymentOperationHistoryDao,
    private val paymentRepository: PaymentRepository,
) : PaymentStatusCheckerService {
    private val logger = loggerFor(javaClass)

    private companion object {
        const val LOG_START_STATUS_CHECK = "Начало проверки статуса платежа. PaymentBankId: %s. TraceId: %s"
        const val LOG_UNSUPPORTED_PAYMENT_TYPE = "Неподдерживаемый тип платежа %s для заказа %s. TraceId: %s"
        const val LOG_OPERATION_HISTORY_ADDED =
            "Запись о проверке статуса добавлена в историю для заказа %s. TraceId: %s"
        const val LOG_PAYMENT_STATUS_RECEIVED = "Получен статус платежа '%s' для заказа %s. TraceId: %s"
        const val LOG_UNKNOWN_PAYMENT_STATUS = "Неизвестный статус платежа: %s для заказа %s. TraceId: %s"

        const val LOG_CARD_PAYMENT_PROCESSING = "Обработка платежа по банковской карте для платежа %s. ID операции: %s"
        const val LOG_GPB_PAYMENT_PROCESSING = "Обработка платежа ГПБ для %s. ID операции: %s"
        const val LOG_GPB_API_CALL = "Отправка запроса статуса платежа в ГПБ. URL: %s. ID операции: %s"
        const val LOG_GPB_API_SUCCESS = "Успешный ответ от API ГПБ для платежа %s. ID операции: %s"
        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции: %s"
        const val LOG_GPB_API_CALL_ERROR = "Произошла ошибка на одном из шагов операции, история сохранена."

        const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        const val ORDERS_NOT_FOUND = "Заказ не найден"
    }

    override fun getStatus(paymentBankId: String): Response<ResponseStatusPay> {
        val traceId = getTraceId()
        logger.info(LOG_START_STATUS_CHECK.format(paymentBankId, traceId))
        val payment =
            paymentDao.findByPaymentBankId(paymentBankId)
        return when (payment.stateId?.stateId) {
            StatusEnum.NEW.value, StatusEnum.SUCCESS.value, StatusEnum.FAIL.value, StatusEnum.REFUND.value, StatusEnum.DECLINED.value -> {
                getSuccessResponse(
                    traceId,
                    1101520200,
                    ResponseStatusPay(
                        paymentStatus = payment.stateId!!.stateId,
                        cheque = checkChequeStatus(payment, traceId),
                    ),
                )
            }

            StatusEnum.REG.value, StatusEnum.WAIT.value, StatusEnum.CALLBACK.value -> {
                processPaymentStatusCheck(payment)
                getSuccessResponse(
                    traceId,
                    1101520200,
                    ResponseStatusPay(
                        paymentStatus = payment.stateId!!.stateId,
                        cheque = checkChequeStatus(payment, traceId),
                    ),
                )
            }

            else -> throw BusinessException(CODE_ERROR_PAYMENT_STATUS, traceId)
        }
    }

    override fun processPaymentStatusCheck(payment: Payment) {
        val traceId = getTraceId()
        when (payment.typeId?.typeId) {
            "sbp", "bankCard" -> {
                if (payment.bank?.bankId == "gpb") {
                    processBankCardPayment(payment)
                }
            }

            else ->
                logger.info(
                    LOG_UNSUPPORTED_PAYMENT_TYPE.format(
                        payment.typeId?.typeId,
                        payment.orderId?.orderId,
                        traceId,
                    ),
                )
        }
    }


    override fun checkStatusOrder(
        orderStatus: OrderStatus?,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
    ) {
        val traceId = getTraceId()
        val status = StatusEnum.fromValue(orderStatus?.stateId)
        if (status != null) {
            when {
                status.isPaidFor() -> {
                    logger.error("${orderStatus?.stateId} $LOG_ORDER_STATUS_SUCCESS $traceId")
                    throw BusinessException(errorCodeIsPaidFor, traceId)
                }

                status.isNotAvailable() -> {
                    logger.error("${orderStatus?.stateId} $LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL $traceId")
                    throw BusinessException(errorCodeIsNotAvailable, traceId)
                }

                else -> {
                }
            }
        }
    }

    private fun processBankCardPayment(payment: Payment) {
        val traceId = getTraceId()
        logger.info(LOG_CARD_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))
        logger.info(LOG_GPB_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))

        try {
            val url =
                "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PAYMENT_PREFIX}${payment.paymentBankId}"
            logger.info(LOG_GPB_API_CALL.format(url, traceId))

            val response =
                restTemplate.defaultRestTemplate().exchange(url, HttpMethod.POST, null, String::class.java).body ?: ""
            val paymentResponse = objectMapper.readValue(response, PaymentStatusResponse::class.java)

            if (response.isNotEmpty()) {
                logger.info(LOG_GPB_API_SUCCESS.format(payment.paymentBankId, traceId))
                updatePaymentStatus(payment, paymentResponse)
            } else {
                logger.error(LOG_GPB_API_ERROR.format(traceId))
                throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
            }
        } catch (e: Exception) {
            logger.info(LOG_GPB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
        }
    }

    private fun updatePaymentStatus(
        payment: Payment,
        response: PaymentStatusResponse,
    ) {
        val traceId = getTraceId()
        val order =
            payment.orderId
                // не менял на дао так как не ясна логика верна или нет попросил рому после протестировать это место
                ?.let { it.id?.let { it1 -> orderRepository.findById(it1) } }
                ?.orElseThrow { InnerException(ORDERS_NOT_FOUND, traceId) }
                ?: throw InnerException(ORDERS_NOT_FOUND, traceId)
        val status = response.result.status

        logger.info(LOG_PAYMENT_STATUS_RECEIVED.format(status, order.orderId, traceId))

        when (status) {
            StatusEnum.SUCCESS.value -> {
                payment.stateId = paymentStatusDao.getPaymentStatus(traceId, StatusEnum.SUCCESS.value)
                payment.orderId?.orderStatus = orderStatusDao.getOrderStatus(traceId, StatusEnum.SUCCESS.value)
                val subOrder = subOrderDao.getSubOrder(traceId, order)
                operationHistoryDao.saveRecordOperationHistory(
                    order,
                    subOrder.clientSystem,
                    traceId,
                    ActionType.ORDER_PAID.value,
                )
                receiptService.generateReceipt(order)
                sendToPaidOrdersQueue(order, traceId)
            }

            StatusEnum.UNKNOWN.value, StatusEnum.INTERIM_SUCCESS.value, StatusEnum.REFUND.value -> {
                payment.stateId = paymentStatusDao.getPaymentStatus(traceId, StatusEnum.WAIT.value)
            }

            StatusEnum.FAILED.value -> {
                payment.stateId = paymentStatusDao.getPaymentStatus(traceId, StatusEnum.FAIL.value)
            }

            StatusEnum.DECLINED.value -> {
                payment.stateId = paymentStatusDao.getPaymentStatus(traceId, StatusEnum.DECLINED.value)
            }

            else -> logger.warn(LOG_UNKNOWN_PAYMENT_STATUS.format(status, order.orderId, traceId))
        }

        payment.updateDate = LocalDateTime.now()
        paymentDao.save(payment)
    }

    /** начало генерации ИИ qwen2.5-coder:14b  */
    private fun sendToPaidOrdersQueue(
        order: Order,
        traceId: String,
    ) {
        try {
            val subOrders = subOrderDao.getAllSubOrderListByOrderId(order, traceId)
            val mainSubOrder =
                subOrders.firstOrNull()
                    ?: throw IllegalStateException("Нет подзаказов для заказа: ${order.id}")

            val requestBody =
                mainSubOrder.clientSystem?.externalSystemCode?.let {
                    PaidOrderMessage(
                        orderId = order.orderId,
                        recipientEmail = order.recipientEmail,
                        externalSystemCode = it,
                        docType = mainSubOrder.docType,
                        policyId = mainSubOrder.policyId,
                        policyNumber = mainSubOrder.policyNumber,
                        contractNumber = mainSubOrder.contractNumber,
                        contractId = mainSubOrder.contractId,
                        typeInsurance = mainSubOrder.typeInsurance,
                        premiumAmount = mainSubOrder.premiumAmount,
                        paySuccess = order.updateDate?.atZone(ZoneOffset.UTC)?.format(DateTimeFormatter.ISO_INSTANT),
                    )
                }

            val paidOrderMessage =
                QueueMessageDto(
                    variables =
                        listOf(
                            VariableDto("ClientID", "payService"),
                            VariableDto("flowCode", "ResultPay"),
                            VariableDto(
                                "requestBody",
                                objectMapper.writeValueAsString(requestBody),
                            ),
                        ),
                )

            rabbitTemplate.convertAndSend(
                "",
                rabbit.template.routingKey,
                paidOrderMessage,
            )

            logger.info(LOG_QUEUE_MESSAGE_SENT.format(order.orderId, traceId))
        } catch (e: Exception) {
            logger.info(LOG_QUEUE_MESSAGE_ERROR + e.message)
            throw InnerException(traceId, LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }

    /** Конец генерации ИИ qwen2.5-coder:14b  */

    private fun checkChequeStatus(
        payment: Payment,
        traceId: String,
    ): Boolean {
        val freshPayment =
            // тоже не трогал просьба глянуть роме
            paymentRepository
                .findById(payment.id!!)
                .orElseThrow { BusinessException(CODE_ERROR_PAYMENT_STATUS, traceId) }
        if (freshPayment.chequeName == "SENT") {
            return true
        }
        return false
    }
}
