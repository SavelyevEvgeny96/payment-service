package ru.sogaz.site.paymentService.service.impl

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
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
import java.time.LocalDateTime
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
    private val configDataDao: ConfigDataDao,
    private val receiptService: ReceiptService,
    private val orderStatusRepository: OrderStatusRepository
) : PaymentStatusCheckerService {

    private val logger = loggerFor(javaClass)

    companion object{
        const val STATUS_PREFIX = "/payment/pay/status/"
    }

    // Основной метод, запускаемый по расписанию
    @Scheduled(fixedDelayString = "\${payment.status.check.interval:60000}")
    override fun checkUnpaidPayments() {
        val traceId = UUID.randomUUID().toString()
        logger.info("Запуск фоновой задачи проверки статусов платежей. ID операции: $traceId")

        try {
            val periodPay = configDataRepository.findByParamName("60000").paramValue.toLong()

            val unpaidOrders = orderRepository.findByStatuses(
                listOf("NEW", "UPDATE")
            )

            logger.info("Найдено ${unpaidOrders.size} неоплаченных заказов для проверки")

            unpaidOrders.forEach { order ->
                try {
                    checkOrderPaymentStatus(order, traceId)
                    Thread.sleep(periodPay)
                } catch (e: Exception) {
                    logger.info(
                        "Ошибка при проверке статуса платежа для заказа ${order.code}. ID операции: $traceId",
                        e
                    )
                }
            }
        } catch (e: Exception) {
            logger.info("Критическая ошибка в фоновой задаче проверки платежей. ID операции: $traceId", e)
        }
    }

    private fun checkOrderPaymentStatus(order: Order, traceId: String) {
        logger.info("Проверка статуса платежа для заказа ${order.code}. ID операции: $traceId")

        val orderFindByCode = orderRepository.findByCode(order.code)

        when (orderFindByCode.orderStatus?.stateId) {
            "SUCCESS", "OVERDUE", "MARKEDDEL" -> {
                logger.info("Заказ ${order.code} уже имеет финальный статус ${orderFindByCode.orderStatus?.stateId}. Пропускаем проверку. ID операции: $traceId")
                return
            }

            "NEW", "UPDATE" -> {
                logger.info("Заказ ${order.code} требует проверки статуса платежа. ID операции: $traceId")
            }

            else -> {
                logger.info("Заказ ${order.code} имеет неожиданный статус ${orderFindByCode.orderStatus?.stateId}. ID операции: $traceId")
                return
            }
        }

        val payment = paymentRepository.findByOrderId(orderFindByCode)
            ?: run {
                logger.error("Платеж для заказа ${order.code} не найден. ID операции: $traceId")
                throw BusinessException(-1101520409, traceId)
            }

        when (payment.typeId?.typeId) {
            "sbp", "bankCard" -> processBankCardPayment(orderFindByCode, payment, traceId)
            else -> {
                logger.info("Неподдерживаемый тип платежа ${payment.typeId?.typeId} для заказа ${order.code}. ID операции: $traceId")
                return
            }
        }
    }

    private fun processBankCardPayment(order: Order, payment: Payment, traceId: String) {
        logger.info("Обработка платежа по банковской карте для заказа ${order.code}. ID операции: $traceId")

        if (order.bankId?.bankId != "gpb") {
            logger.info("Заказ ${order.code} не относится к ГПБ (банк: ${order.bankId?.bankId}). В MVP не обрабатываем. ID операции: $traceId")
            return
        }

        logger.info("Обработка платежа ГПБ для заказа ${order.code}. ID операции: $traceId")

        val orderFindByCode =
            try {
                orderRepository.findByCode(order.code)
            } catch (e: Exception) {
                logger.error(e, PaymentServiceImpl.LOG_NOT_FOUND_ORDER_TO_CODE, order.code, traceId)
                throw BusinessException(CustomPaymentErrors.CODE_ERROR_ORDER_NOT_FOUND, traceId)
            }

        val tokenGpb = configDataDao.getGPBToken(traceId, orderFindByCode)
        if (tokenGpb.isNotEmpty()) {
            val actionTypeTokenSuccess =
                try {
                    actionTypeRepository.findByActionName(PaymentServiceImpl.GET_TOKEN_MASSAGE_SUCCESS)
                } catch (e: Exception) {
                    logger.error(e, PaymentServiceImpl.LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
                    throw InnerException(traceId, PaymentServiceImpl.LOG_AND_ERROR_FIND_ACTION_TYPE)
                }

            val url =
                "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}${PaymentServiceImpl.PAYMENT_PREFIX}${tokenGpb}${STATUS_PREFIX}${order.code}"
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            try {
                logger.info("Отправка запроса статуса платежа в ГПБ. URL: $url. ID операции: $traceId")

                val response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                    object : ParameterizedTypeReference<Map<String, Any>>() {}
                )

                if (response.statusCode == HttpStatus.OK) {
                    logger.info("Успешный ответ от API ГПБ для заказа ${order.code}. ID операции: $traceId")
                    processSuccessfulResponse(response.body, order, payment, traceId)
                } else {
                    logger.error("Ошибка при запросе статуса в ГПБ. Код ответа: ${response.statusCode}. ID операции: $traceId")
                    throw BusinessException(-1101520504, traceId)
                }
            } catch (e: Exception) {
                logger.info("Ошибка при вызове API ГПБ для заказа ${order.code}. ID операции: $traceId", e)
                throw BusinessException(-1101520504, traceId)
            }
        }
    }

        private fun processSuccessfulResponse(
            responseBody: Map<String, Any>?,
            order: Order,
            payment: Payment,
            traceId: String
        ) {
            logger.info("Обработка успешного ответа для заказа ${order.code}. ID операции: $traceId")

            val actionType = actionTypeRepository.findByActionName("GET_PAYMENT_STATUS_SUCCESS")
            val subOrder = subOrderRepository.findFirstByOrderId(order)
            val operationHistory = PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now()
            )
            operationHistoryRepository.save(operationHistory)
            logger.info("Запись о проверке статуса добавлена в историю для заказа ${order.code}. ID операции: $traceId")


            val result = responseBody?.get("result") as? Map<String, Any>
            val status = result?.get("status") as? String

            logger.info("Получен статус платежа '$status' для заказа ${order.code}. ID операции: $traceId")

            when (status) {
                "SUCCESS" -> handleSuccessStatus(order, payment, traceId)
                "UNKNOWN", "INTERIM_SUCCESS", "REFUND" -> updatePaymentStatus(payment, "WAIT", traceId)
                "FAILED" -> updatePaymentStatus(payment, "FAIL", traceId)
                "DECLINED" -> updatePaymentStatus(payment, "DECLINED", traceId)
                else -> logger.warn("Неизвестный статус платежа: $status для заказа ${order.code}. ID операции: $traceId")
            }
        }

        private fun handleSuccessStatus(order: Order, payment: Payment, traceId: String) {
            logger.info("Обработка успешного платежа для заказа ${order.code}. ID операции: $traceId")

            val successStatus = paymentStatusRepository.findByStateId("SUCCESS")
            payment.stateId = successStatus
            payment.updateDate = LocalDateTime.now()
            paymentRepository.save(payment)
            logger.info("Статус платежа обновлен на SUCCESS для заказа ${order.code}. ID операции: $traceId")


            val orderSuccessStatus = orderStatusRepository.findByStateId("SUCCESS")
            order.orderStatus = orderSuccessStatus
            order.updateDate = LocalDateTime.now()
            orderRepository.save(order)
            logger.info("Статус заказа обновлен на SUCCESS для заказа ${order.code}. ID операции: $traceId")


            val actionType = actionTypeRepository.findByActionName("ORDER_PAID")
            val subOrder = subOrderRepository.findFirstByOrderId(order)
            val operationHistory = PaymentOperationHistory(
                action = actionType,
                order = order,
                actionAuthor = subOrder.clientSystem,
                actionDate = LocalDateTime.now()
            )
            operationHistoryRepository.save(operationHistory)
            logger.info("Запись об успешной оплате добавлена в историю для заказа ${order.code}. ID операции: $traceId")


            if (order.needReceipt == true) {
                logger.info("Заказ ${order.code} требует генерации чека. ID операции: $traceId")
                generateReceipt(order, traceId) // чеки
            }
        }

        private fun updatePaymentStatus(payment: Payment, status: String, traceId: String) {
            logger.info("Обновление статуса платежа на $status для платежа ${payment.id}. ID операции: $traceId")
            val newStatus = paymentStatusRepository.findByStateId(status)
            payment.stateId = newStatus
            payment.updateDate = LocalDateTime.now()
            paymentRepository.save(payment)
        }


        private fun generateReceipt(order: Order, traceId: String) {
            logger.info("Запуск генерации чека для заказа ${order.code}. ID операции: $traceId")
            try {
                receiptService.generateReceipt(order)
                logger.info("Чек для заказа ${order.code} успешно сгенерирован. ID операции: $traceId")
            } catch (e: Exception) {
                logger.info("Ошибка генерации чека для заказа ${order.code}. ID операции: $traceId", e)

                val actionType = actionTypeRepository.findByActionName("RECEIPT_GENERATION_ERROR")
                val subOrder = subOrderRepository.findFirstByOrderId(order)
                val operationHistory = PaymentOperationHistory(
                    action = actionType,
                    order = order,
                    actionAuthor = subOrder.clientSystem,
                    actionDate = LocalDateTime.now()
                )
                operationHistoryRepository.save(operationHistory)
                logger.info("Ошибка генерации чека записана в историю для заказа ${order.code}. ID операции: $traceId")
            }
        }
    }