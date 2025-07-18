package ru.sogaz.site.paymentService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dto.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.PaymentStatusResponse
import ru.sogaz.site.paymentService.dto.QueueMessageDto
import ru.sogaz.site.paymentService.dto.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.VariableDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
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
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class PaymentStatusCheckerServiceImpl(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val configDataRepository: ConfigDataRepository,
    private val paymentStatusRepository: PaymentStatusRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val restTemplate: RestTemplate,
    private val subOrderRepository: SubOrderRepository,
    private val apiConfigProperty: ApiConfigProperty,
    private val receiptService: ReceiptService,
    private val orderStatusRepository: OrderStatusRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val rabbit: RabbitProperties,
) : PaymentStatusCheckerService {
    private val logger = loggerFor(javaClass)

    private companion object {
        const val LOG_START_STATUS_CHECK = "Начало проверки статуса платежа. PaymentBankId: %s. TraceId: %s"
        const val LOG_PAYMENT_NOT_FOUND = "Платеж для заказа %s не найден. TraceId: %s"
        const val LOG_UNSUPPORTED_PAYMENT_TYPE = "Неподдерживаемый тип платежа %s для заказа %s. TraceId: %s"
        const val LOG_OPERATION_HISTORY_ADDED =
            "Запись о проверке статуса добавлена в историю для заказа %s. TraceId: %s"
        const val LOG_PAYMENT_STATUS_RECEIVED = "Получен статус платежа '%s' для заказа %s. TraceId: %s"
        const val LOG_UNKNOWN_PAYMENT_STATUS = "Неизвестный статус платежа: %s для заказа %s. TraceId: %s"

        const val LOG_BACKGROUND_TASK_START = "Запуск фоновой задачи проверки статусов платежей. ID операции: %s"
        const val LOG_UNPAID_PAYMENTS_FOUND = "Найдено %d неоплаченных платежей для проверки"
        const val LOG_PAYMENT_CHECK_ERROR = "Ошибка при проверке статуса платежа для заказа %s. ID операции: %s"
        const val LOG_CRITICAL_TASK_ERROR = "Критическая ошибка в фоновой задаче проверки платежей. ID операции: %s"

        const val LOG_CARD_PAYMENT_PROCESSING = "Обработка платежа по банковской карте для платежа %s. ID операции: %s"
        const val LOG_GPB_PAYMENT_PROCESSING = "Обработка платежа ГПБ для %s. ID операции: %s"
        const val LOG_GPB_API_CALL = "Отправка запроса статуса платежа в ГПБ. URL: %s. ID операции: %s"
        const val LOG_GPB_API_SUCCESS = "Успешный ответ от API ГПБ для платежа %s. ID операции: %s"
        const val LOG_GPB_API_ERROR = "Ошибка при запросе статуса в ГПБ. ID операции: %s"
        const val LOG_GPB_API_CALL_ERROR = "Ошибка при вызове API ГПБ для %s. ID операции: %s"

        const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        const val ORDERS_NOT_FOUND = "Заказ не найден"
    }

    override fun getStatus(
        payment_bank_id: String,
        traceId: String,
    ): Response<ResponseStatusPay> {
        logger.info(LOG_START_STATUS_CHECK.format(payment_bank_id, traceId))

        val payment =
            paymentRepository.findByPaymentBankId(payment_bank_id)
                ?: throw BusinessException(-1101520409, traceId).also {
                    logger.error(LOG_PAYMENT_NOT_FOUND.format(payment_bank_id, traceId))
                }

        val chequeStatus =
            if (payment.stateId?.stateId == "SUCCESS") {
                checkChequeStatus(payment, traceId)
            } else {
                false
            }

        return when (payment.stateId?.stateId) {
            "NEW", "SUCCESS", "FAIL", "REFUND", "DECLINED" -> {
                getSuccessResponse(
                    traceId,
                    1101520200,
                    ResponseStatusPay(
                        paymentStatus = payment.stateId!!.stateId,
                        cheque = chequeStatus,
                    ),
                )
            }

            "REG", "WAIT", "CALLBACK" -> {
                processPaymentStatusCheck(payment, traceId)
                getSuccessResponse(
                    traceId,
                    1101520200,
                    ResponseStatusPay(
                        paymentStatus = payment.stateId!!.stateName,
                        cheque = chequeStatus,
                    ),
                )
            }

            else -> throw BusinessException(-1101520409, traceId)
        }
    }

    @Scheduled(fixedDelayString = "60000")
    override fun checkUnpaidPayments() {
        val traceId = UUID.randomUUID().toString()
        logger.info(LOG_BACKGROUND_TASK_START.format(traceId))

        try {
            val periodPay = configDataRepository.findByParamName("periodPay").paramValue.toLong()

            val unpaidOrders = paymentRepository.findByStatuses(listOf("REG", "WAIT", "CALLBACK"))

            logger.info(LOG_UNPAID_PAYMENTS_FOUND.format(unpaidOrders.size))

            unpaidOrders.forEach { payment ->
                try {
                    processPaymentStatusCheck(payment, traceId)
                    Thread.sleep(periodPay)
                } catch (e: Exception) {
                    logger.info(LOG_PAYMENT_CHECK_ERROR.format(payment.paymentBankId, traceId), e)
                }
            }
        } catch (e: Exception) {
            logger.info(LOG_CRITICAL_TASK_ERROR.format(traceId), e)
        }
    }

    private fun processPaymentStatusCheck(
        payment: Payment,
        traceId: String,
    ) {
        when (payment.typeId?.typeId) {
            "sbp", "bankCard" -> {
                if (payment.bank?.bankId == "gpb") {
                    processBankCardPayment(payment, traceId)
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

    private fun processBankCardPayment(
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
                throw BusinessException(-1101520504, traceId)
            }
        } catch (e: Exception) {
            logger.info(LOG_GPB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
            throw BusinessException(-1101520504, traceId)
        }
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
                createOrderHistoryRecord(order, "Заказ оплачен", traceId)

                if (order.needReceipt == true) {
                    receiptService.generateReceipt(order, traceId)
                    sendToPaidOrdersQueue(order, traceId)
                }
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
                        recipientPhone = order.recipientPhone,
                        recipientUserId = order.recipientUserId,
                        externalSystemCode = it,
                        docType = mainSubOrder.docType,
                        policyId = mainSubOrder.policyId,
                        policyNumber = mainSubOrder.policyNumber,
                        contractNumber = mainSubOrder.contractNumber,
                        contractId = mainSubOrder.contractId,
                        typeInsurance = mainSubOrder.typeInsurance,
                        premiumAmount = mainSubOrder.premiumAmount,
                        managerEmail = mainSubOrder.managerEmail,
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
                .orElseThrow { BusinessException(-1101520409, traceId) }
        if (freshPayment.chequeName == "SENT") {
            return true
        }
        val order = freshPayment.orderId ?: return false
        if (order.needReceipt != true) {
            return false
        }
        if (freshPayment.stateId?.stateId != "SUCCESS") {
            return false
        }
        return true
    }
}
