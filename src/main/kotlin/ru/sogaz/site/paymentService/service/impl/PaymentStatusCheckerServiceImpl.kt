package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS_BANK
import ru.sogaz.site.paymentService.dto.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.dto.PaymentStatusResponse
import ru.sogaz.site.paymentService.dto.QueueMessageDto
import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.VariableDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl.Companion.LOG_ORDER_STATUS_SUCCESS
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class PaymentStatusCheckerServiceImpl(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentStatusRepository: PaymentStatusRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val restTemplate: RestTemplate,
    private val subOrderRepository: SubOrderRepository,
    private val apiConfigProperty: ApiConfigProperties,
    private val receiptService: ReceiptService,
    private val orderStatusRepository: OrderStatusRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val rabbit: RabbitProperties,
) : PaymentStatusCheckerService {
    private val logger = loggerFor(javaClass)

    private companion object {
        const val LOG_START_STATUS_CHECK = "Начало проверки статуса платежа. PaymentBankId: %s. TraceId: %s"
        const val LOG_AKB_API_CALL = "Отправка запроса статуса платежа в АКБ. URL: %s. ID операции: %s"
        const val LOG_UNSUPPORTED_PAYMENT_TYPE = "Неподдерживаемый тип платежа %s для заказа %s. TraceId: %s"
        const val LOG_OPERATION_HISTORY_ADDED =
            "Запись о проверке статуса добавлена в историю для заказа %s. TraceId: %s"
        const val LOG_PAYMENT_STATUS_RECEIVED = "Получен статус платежа '%s' для заказа %s. TraceId: %s"
        const val LOG_UNKNOWN_PAYMENT_STATUS = "Неизвестный статус платежа: %s для заказа %s. TraceId: %s"
        const val LOG_AKB_PAYMENT_PROCESSING = "Обработка платежа АКБ для %s. ID операции: %s"
        const val LOG_CARD_PAYMENT_PROCESSING = "Обработка платежа по банковской карте для платежа %s. ID операции: %s"
        const val LOG_GPB_PAYMENT_PROCESSING = "Обработка платежа ГПБ для %s. ID операции: %s"
        const val LOG_GPB_API_CALL = "Отправка запроса статуса платежа в ГПБ. URL: %s. ID операции: %s"
        const val LOG_GPB_API_SUCCESS = "Успешный ответ от API ГПБ для платежа %s. ID операции: %s"
        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции: %s"
        const val LOG_GPB_API_CALL_ERROR = "Произошла ошибка на одном из шагов операции, история сохранена."
        const val LOG_AKB_API_SUCCESS = "Успешный ответ от API АКБ для платежа %s. ID операции: %s"
        const val LOG_AKB_API_ERROR = "Ошибка при запросе статуса в АКБ. ID операции: %s"
        const val LOG_AKB_API_CALL_ERROR = "Произошла ошибка на одном из шагов операции, история сохранена."
        const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        const val ORDERS_NOT_FOUND = "Заказ не найден"
        const val ORDER_SUCCESS = "Заказ оплачен"
    }

    override fun getStatus(
        paymentBankId: String,
        traceId: String,
    ): Response<ResponseStatusPay> {
        logger.info(LOG_START_STATUS_CHECK.format(paymentBankId, traceId))

        val payment =
            paymentRepository.findByPaymentBankId(paymentBankId)

        return when (payment.stateId?.stateId) {
            "NEW", "SUCCESS", "FAIL", "REFUND", "DECLINED" -> {
                getSuccessResponse(
                    traceId,
                    1101520200,
                    ResponseStatusPay(
                        paymentStatus = payment.stateId!!.stateId,
                        cheque = checkChequeStatus(payment, traceId),
                    ),
                )
            }

            "REG", "WAIT", "CALLBACK" -> {
                processPaymentStatusCheck(payment, traceId)
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

    override fun processPaymentStatusCheck(
        payment: Payment,
        traceId: String,
    ) {
        when (payment.typeId?.typeId) {
            "sbp", "bankCard" -> {
                if (payment.bank?.bankId == "gpb") {
                    processBankCardPaymentGpb(payment, traceId)
                } else if (payment.bank?.bankId == "akb_rus"){
                    processBankCardPaymentAkb(payment, traceId)
                }
            }

            else ->
                logger.info(
                    LOG_UNSUPPORTED_PAYMENT_TYPE.format(
                        payment.typeId?.typeId,
                        payment.orderId?.code,
                        traceId,
                    ),
                )
        }
    }

    override fun checkStatusOrder(
        orderStatus: OrderStatus?,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
        traceId: String,
    ) {
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

    private fun processBankCardPaymentAkb(
        payment: Payment,
        traceId: String,
    ) {
        logger.info(LOG_CARD_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))
        logger.info(LOG_AKB_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))

        try {
            val url = apiConfigProperty.akbUrl + payment.paymentBankId + "?password=" + payment.payment +
                "&orderDetailLevel=2&" +
                    "tranDetailLevel=2&" +
                    "actionDetailLevel=2&" +
                    "cofpDetailLevel=2&" +
                    "consumerDetailLevel=2&" +
                    "consumerTokenDetailLevel=2&" +
                    "tokenDetailLevel=2"
            logger.info(LOG_AKB_API_CALL.format(url, traceId))

            val response = restTemplate.exchange(url, HttpMethod.POST, null, String::class.java).body ?: ""
            val paymentResponse = objectMapper.readValue(response, PaymentAkbStatusResponse::class.java)

            if (paymentResponse.status == "Closed") {
                logger.info(LOG_AKB_API_SUCCESS.format(payment.paymentBankId, traceId))
                updateAkbPaymentStatus(payment, paymentResponse, traceId)
            } else {
                logger.error(LOG_AKB_API_ERROR.format(traceId))
                throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
            }
        } catch (e: Exception) {
            logger.info(LOG_AKB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
        }
    }

    private fun processBankCardPaymentGpb(
        payment: Payment,
        traceId: String,
    ) {
        logger.info(LOG_CARD_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))
        logger.info(LOG_GPB_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))

        try {
            val url =
                "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.PAYMENT_PREFIX}${payment.paymentBankId}"
            logger.info(LOG_GPB_API_CALL.format(url, traceId))

            val response = restTemplate.exchange(url, HttpMethod.POST, null, String::class.java).body ?: ""
            val paymentResponse = objectMapper.readValue(response, PaymentStatusResponse::class.java)

            if (response.isNotEmpty()) {
                logger.info(LOG_GPB_API_SUCCESS.format(payment.paymentBankId, traceId))
                updatePaymentStatus(payment, paymentResponse, traceId)
            } else {
                logger.error(LOG_GPB_API_ERROR.format(traceId))
                throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
            }
        } catch (e: Exception) {
            logger.info(LOG_GPB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
        }
    }

    private fun updateAkbPaymentStatus(
        payment: Payment,
        response: PaymentAkbStatusResponse,
        traceId: String,
    ): Payment {
        val order = payment.orderId
            ?.let { it.id?.let { id -> orderRepository.findById(id) } }
            ?.orElseThrow { InnerException(ORDERS_NOT_FOUND, traceId) }
            ?: throw InnerException(ORDERS_NOT_FOUND, traceId)

        val currentTime = LocalDateTime.now()
        payment.updateDate = currentTime

        when (response.prevStatus) {
            "Preparing", "WaitPushTran", "Authorized" -> {
                payment.stateId = paymentStatusRepository.findByStateId("WAIT")
            }
            "PartPaid", "Refunded", "Voided" -> {
                payment.stateId = paymentStatusRepository.findByStateId("REFUND")
            }
            "Declined", "Expired" -> {
                payment.stateId = paymentStatusRepository.findByStateId("FAIL")
            }
            "Refused" -> {
                payment.stateId = paymentStatusRepository.findByStateId("DECLINED")
            }
            "FullyPaid" -> {
                payment.stateId = paymentStatusRepository.findByStateId("SUCCESS")
                order.orderStatus = orderStatusRepository.findByStateId("SUCCESS")
                order.updateDate = currentTime
                orderRepository.save(order)
                createOrderHistoryRecord(order, ORDER_SUCCESS, traceId)
                receiptService.generateReceipt(order, traceId)
                sendToPaidOrdersQueue(order, traceId)
            }
            else -> {
                logger.warn("Unknown prevStatus: ${response.prevStatus}")
                payment.stateId = paymentStatusRepository.findByStateId("FAIL")
            }
        }

        return paymentRepository.save(payment)
    }

    private fun updatePaymentStatus(
        payment: Payment,
        response: PaymentStatusResponse,
        traceId: String,
    ) {
        val order =
            payment.orderId
                ?.let { it.id?.let { it1 -> orderRepository.findById(it1) } }
                ?.orElseThrow { InnerException(ORDERS_NOT_FOUND, traceId) }
                ?: throw InnerException(ORDERS_NOT_FOUND, traceId)
        val status = response.result.status

        logger.info(LOG_PAYMENT_STATUS_RECEIVED.format(status, order.code, traceId))

        when (status) {
            "SUCCESS" -> {
                payment.stateId = paymentStatusRepository.findByStateId("SUCCESS")
                payment.orderId?.orderStatus = orderStatusRepository.findByStateId("SUCCESS")
                createOrderHistoryRecord(order, ORDER_SUCCESS, traceId)

                receiptService.generateReceipt(order, traceId)
                sendToPaidOrdersQueue(order, traceId)
            }

            "UNKNOWN", "INTERIM_SUCCESS", "REFUND" -> {
                payment.stateId = paymentStatusRepository.findByStateId("WAIT")
            }

            "FAILED" -> {
                payment.stateId = paymentStatusRepository.findByStateId("FAIL")
            }

            "DECLINED" -> {
                payment.stateId = paymentStatusRepository.findByStateId("DECLINED")
            }

            else -> logger.warn(LOG_UNKNOWN_PAYMENT_STATUS.format(status, order.code, traceId))
        }

        payment.updateDate = LocalDateTime.now()
        paymentRepository.save(payment)
    }

    private fun createOrderHistoryRecord(
        order: Order,
        action: String,
        traceId: String,
    ) {
        val actionType = actionTypeRepository.findByActionName(action)
        val subOrder = subOrderRepository.findFirstByOrderId(order)

        val historyRecord =
            PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now(),
            )

        operationHistoryRepository.save(historyRecord)
        logger.info(LOG_OPERATION_HISTORY_ADDED.format(order.code, traceId))
    }

    /** начало генерации ИИ qwen2.5-coder:14b  */
    private fun sendToPaidOrdersQueue(
        order: Order,
        traceId: String,
    ) {
        try {
            val subOrders = subOrderRepository.findAllByOrderId(order)
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

            logger.info(LOG_QUEUE_MESSAGE_SENT.format(order.code, traceId))
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
            paymentRepository
                .findById(payment.id!!)
                .orElseThrow { BusinessException(CODE_ERROR_PAYMENT_STATUS, traceId) }
        if (freshPayment.chequeName == "SENT") {
            return true
        }
        return false
    }
}
