package ru.sogaz.site.paymentService.service.payment

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS_BANK
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.ClientCardDetails
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.data.SubOrderPayload
import ru.sogaz.site.paymentService.dto.response.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.ChequeStateEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.HistoryService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class PaymentStatusCheckerServiceImpl(
    private val paymentDao: PaymentDao,
    private val restTemplate: WebConfigRestTemplate,
    private val apiConfigProperty: ApiConfigProperties,
    private val receiptService: ReceiptService,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val props: RabbitProperties,
    private val subOrderDao: SubOrderDao,
    private val operationHistoryDao: PaymentOperationHistoryDao,
    private val orderDao: OrderDao,
    private val historyService: HistoryService,
) : PaymentStatusCheckerService {
    private val logger = loggerFor(javaClass)

    private companion object {
        const val LOG_START_STATUS_CHECK = "Начало проверки статуса платежа. PaymentBankId: %s. TraceId: %s"
        const val LOG_AKB_API_CALL = "Отправка запроса статуса платежа в АКБ. URL: %s. ID операции: %s"
        const val LOG_UNSUPPORTED_PAYMENT_TYPE = "Неподдерживаемый тип платежа %s для заказа %s. TraceId: %s"
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
        const val LOG_ORDER_STATUS_SUCCESS = "Ошибка совершения платежа. Указанный заказ уже оплачен"
        const val LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL =
            "Ошибка совершения платежа. Указанный заказ не доступен для оплаты"
        const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val START_LOG_MESSAGE_QUEUE = "Старт записи в очередь routingKey: %s  exchange: %s "
        const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        const val ORDERS_NOT_FOUND = "Заказ не найден"
        const val ORDER_SUCCESS = "Заказ оплачен"
        const val PAYMENT_PREFIX = "/payment/"
        const val CONST_PASSWORD = "?password="
        const val CONST_URL_AKB =
            "&orderDetailLevel=2&" +
                "tranDetailLevel=2&" +
                "actionDetailLevel=2&" +
                "cofpDetailLevel=2&" +
                "consumerDetailLevel=2&" +
                "consumerTokenDetailLevel=2&" +
                "tokenDetailLevel=2"
    }

    override fun getStatus(paymentBankId: String): Response<ResponseStatusPay> {
        val traceId = getTraceId()
        logger.info(LOG_START_STATUS_CHECK.format(paymentBankId, traceId))
        val payment = paymentDao.findByPaymentBankId(paymentBankId)
        logger.info(payment.state.name)
        when (payment.state) {
            PaymentStatusEnum.REG,
            PaymentStatusEnum.WAIT,
            PaymentStatusEnum.CALLBACK,
            -> processPaymentStatusCheck(payment)

            else -> {}
        }
        return getSuccessResponse(
            traceId,
            1101520200,
            responseStatusPay(payment, traceId),
        )
    }

    private fun responseStatusPay(
        payment: Payment,
        traceId: String,
    ): ResponseStatusPay {
        val payments =
            payment.paymentBankId?.let { paymentDao.findByPaymentBankId(it) }
        return ResponseStatusPay(
            paymentStatus = payments!!.state,
            cheque = checkChequeStatus(payments, traceId),
        )
    }

    override fun processPaymentStatusCheck(payment: Payment) {
        val traceId = getTraceId()
        when (payment.type) {
            PaymentTypeEnum.SBP,
            PaymentTypeEnum.CARD,
            -> {
                if (payment.bank == BankEnum.GPB) {
                    getStatusBankCardPaymentGpb(payment, traceId)
                } else if (payment.bank == BankEnum.AKB_RUS) {
                    getStatusBankCardPaymentAkb(payment, traceId)
                }
            }
            else ->
                logger.info(
                    LOG_UNSUPPORTED_PAYMENT_TYPE.format(
                        payment.type,
                        payment.order?.id,
                        traceId,
                    ),
                )
        }
    }

    override fun checkStatusOrder(
        orderStatus: OrderStatus,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
    ) {
        val traceId = getTraceId()
        if (orderStatus.isPaidFor()) {
            logger.error("$orderStatus $LOG_ORDER_STATUS_SUCCESS $traceId")
            throw BusinessException(errorCodeIsPaidFor, traceId)
        }
        if (orderStatus.isNotAvailable()) {
            logger.error("$orderStatus $LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL $traceId")
            throw BusinessException(errorCodeIsNotAvailable, traceId)
        }
    }

    private fun getStatusBankCardPaymentAkb(
        payment: Payment,
        traceId: String,
    ) {
        logger.info(LOG_CARD_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))
        logger.info(LOG_AKB_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))

        try {
            val url =
                apiConfigProperty.akbUrl + "/" + payment.paymentBankId + CONST_PASSWORD + payment.paymentPass +
                    CONST_URL_AKB
            logger.info(LOG_AKB_API_CALL.format(url, traceId))

            val response =
                restTemplate.defaultRestTemplate().exchange(url, HttpMethod.GET, null, String::class.java).body ?: ""
            val paymentResponse = objectMapper.readValue(response, PaymentAkbStatusResponse::class.java)

            if (paymentResponse.order.prevStatus?.name == "Closed") {
                logger.info(LOG_AKB_API_SUCCESS.format(payment.paymentBankId, traceId))
                updateAkbPaymentStatus(payment, paymentResponse)
            } else {
                logger.error(LOG_AKB_API_ERROR.format(traceId))
                throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
            }
        } catch (e: Exception) {
            logger.info(LOG_AKB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
        }
    }

    private fun getStatusBankCardPaymentGpb(
        payment: Payment,
        traceId: String,
    ) {
        logger.info(LOG_CARD_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))
        logger.info(LOG_GPB_PAYMENT_PROCESSING.format(payment.paymentBankId, traceId))

        try {
            if (payment.type == PaymentTypeEnum.CARD) {
                val url =
                    "${apiConfigProperty.gpbUrl}${apiConfigProperty.portalId}$PAYMENT_PREFIX${payment.paymentBankId}"
                logger.info(LOG_GPB_API_CALL.format(url, traceId))

                val response =
                    restTemplate
                        .defaultRestTemplate()
                        .exchange(url, HttpMethod.POST, null, String::class.java)
                        .body
                        .orEmpty()

                if (response.isNotEmpty()) {
                    logger.info(LOG_GPB_API_SUCCESS.format(payment.paymentBankId, traceId))

                    val paymentResponse = objectMapper.readValue(response, GpbCardPaymentStatusResponse::class.java)
                    updatePaymentStatus(payment, paymentResponse)
                    val cardDetails = paymentResponse.toGpbCardDetails()
                    logger.info("GPB card details: $cardDetails")
                } else {
                    logger.error(LOG_GPB_API_ERROR.format(traceId))
                    throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
                }
            } else {
                val url = apiConfigProperty.gpbSbpUrlStatus

                val requestBody = objectMapper.writeValueAsString(mapOf("qrcIds" to listOf(payment.qrcId)))

                val headers = HttpHeaders()
                headers.contentType = org.springframework.http.MediaType.APPLICATION_JSON
                val requestEntity = HttpEntity(requestBody, headers)

                val response: String =
                    restTemplate
                        .defaultRestTemplate()
                        .exchange(
                            url,
                            HttpMethod.POST,
                            requestEntity,
                            String::class.java,
                        ).body ?: ""

                if (response.isNotEmpty()) {
                    logger.info(LOG_GPB_API_SUCCESS.format(payment.paymentBankId, traceId))
                    val paymentResponse = objectMapper.readValue(response, GpbCardPaymentStatusResponse::class.java)
                    updatePaymentStatusSbp(payment, paymentResponse)
                } else {
                    logger.error(LOG_GPB_API_ERROR.format(traceId))
                    throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK, traceId)
                }
            }
        } catch (e: Exception) {
            logger.info(LOG_GPB_API_CALL_ERROR.format(payment.paymentBankId, traceId), e)
        }
    }

    private fun GpbCardPaymentStatusResponse.toGpbCardDetails(): ClientCardDetails {
        val maskedPan = gpbCardDetails?.pan
        val paymentSystem = gpbCardDetails?.paymentSystem
        val issuerName = gpbCardDetails?.issuerName
        val paymentType = portalType

        return ClientCardDetails(
            maskedPan = maskedPan,
            paymentSystem = paymentSystem,
            issuerName = issuerName,
            paymentType = paymentType,
            cardId = gpbCardDetails?.cardId,
        )
    }

    private fun String.toGpbCardDetails(objectMapper: ObjectMapper): ClientCardDetails {
        val parsed = objectMapper.readValue(this, GpbCardPaymentStatusResponse::class.java)
        return parsed.toGpbCardDetails()
    }

    private fun updateAkbPaymentStatus(
        payment: Payment,
        response: PaymentAkbStatusResponse,
    ) {
        val order: Order = payment.order ?: return

        payment.state = response.order.prevStatus?.toPaymentStatusesEnum() ?: PaymentStatusEnum.FAIL
        payment.paymentFinished = LocalDateTime.now()
        if (payment.state == PaymentStatusEnum.SUCCESS) {
            updateOrderForSuccessPayment(payment, null)
        }
        paymentDao.save(payment)
    }

    private fun updatePaymentStatusSbp(
        payment: Payment,
        response: GpbCardPaymentStatusResponse,
    ) {
        val traceId = getTraceId()
        val order: Order = payment.order ?: return

        val status = response.result?.status

        logger.info(LOG_PAYMENT_STATUS_RECEIVED.format(status, order.id, traceId))

        try {
            payment.state = status?.toPaymentStatusesEnum() ?: PaymentStatusEnum.WAIT
            payment.paymentFinished = LocalDateTime.now()
        } catch (ex: InnerException) {
            logger.warn(LOG_UNKNOWN_PAYMENT_STATUS.format(status, order.id, traceId))
        }
        if (payment.state == PaymentStatusEnum.SUCCESS) {
            updateOrderForSuccessPayment(payment, response)
        }

        paymentDao.save(payment)
    }

    private fun updateOrderForSuccessPayment(
        payment: Payment,
        paymentStatusResponse: GpbCardPaymentStatusResponse?,
    ) {
        payment.order
            ?.apply { status = OrderStatus.SUCCESS }
            ?.let {
                orderDao.save(it)
                receiptService.generateReceipt(payment)
                historyService.createOrderHistoryRecord(it, getTraceId())
                sendToPaidOrdersQueue(it, getTraceId(), paymentStatusResponse)
            }
    }

    private fun updatePaymentStatus(
        payment: Payment,
        response: GpbCardPaymentStatusResponse,
    ) {
        val traceId = getTraceId()
        val status = response.result?.status
        val order = payment.order ?: return

        logger.info(LOG_PAYMENT_STATUS_RECEIVED.format(status, order.id, traceId))

        payment.state = status?.toPaymentStatusesEnum() ?: PaymentStatusEnum.WAIT
        payment.paymentFinished = LocalDateTime.now()

        if (payment.state == PaymentStatusEnum.SUCCESS) {
            updateOrderForSuccessPayment(payment, response)
        }

        paymentDao.save(payment)
    }

    private fun sendToPaidOrdersQueue(
        order: Order,
        traceId: String,
        paymentStatusResponse: GpbCardPaymentStatusResponse?,
    ) {
        try {
            val subOrders = subOrderDao.getAllSubOrderListByOrderId(order)
            val subOrderPayloads =
                subOrders?.map { s ->
                    SubOrderPayload(
                        s.docType,
                        s.policyId,
                        s.policyNumber,
                        s.contractNumber,
                        s.contractId,
                        s.typeInsurance,
                        s.premiumAmount,
                        s.channel,
                        s.policyDate,
                        s.contractDate,
                    )
                }

            val requestBody =
                PaidOrderMessage(
                    orderId = order.id?.toString(),
                    recipientEmail = order.recipientEmail,
                    externalSystemCode = order.clientId,
                    subscriptionId = order.subscriptionId,
                    paySuccess =
                        order.updateDate
                            ?.atZone(ZoneOffset.UTC)
                            ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    subOrders = subOrderPayloads,
                    paymentStatusResponse?.gpbCardDetails?.issuerName,
                    paymentStatusResponse?.gpbCardDetails?.paymentSystem,
                    paymentStatusResponse?.gpbCardDetails?.pan,
                    paymentStatusResponse?.gpbCardDetails?.type,
                    paymentStatusResponse?.gpbCardDetails?.cardId,
                )
            val exchange = props.exchange
            val routingKey = props.routingKeyStatusPayment
            val timestamp =
                OffsetDateTime
                    .now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            logger.info(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
            rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                requestBody,
            ) { message ->
                message.messageProperties.headers["author"] = "payService"
                message.messageProperties.headers["flowCode"] = "ResultPay"
                message.messageProperties.headers["timestamp"] = timestamp
                message
            }

            logger.info(LOG_QUEUE_MESSAGE_SENT.format(order.id, traceId))
        } catch (e: Exception) {
            logger.error(LOG_QUEUE_MESSAGE_ERROR + e.message, e)
            throw InnerException(traceId, LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }

    private fun checkChequeStatus(
        payment: Payment,
        traceId: String,
    ): Boolean {
        val freshPayment =
            payment.paymentBankId?.let {
                paymentDao
                    .findByPaymentBankId(it)
            }

        return freshPayment?.chequeName == ChequeStateEnum.SENT.name
    }

    private fun StatusEnum.toPaymentStatusesEnum(): PaymentStatusEnum =
        when (this) {
            StatusEnum.NOTSTARTED,
            StatusEnum.RECEIVED,
            StatusEnum.UNKNOWN,
            StatusEnum.INTERIM_SUCCESS,
            StatusEnum.REFUND,
            -> PaymentStatusEnum.WAIT

            StatusEnum.BLOCKED,
            StatusEnum.REJECTED,
            StatusEnum.FAILED,
            -> PaymentStatusEnum.FAIL

            StatusEnum.DECLINED -> PaymentStatusEnum.DECLINED
            StatusEnum.SUCCESS,
            StatusEnum.ACCEPTED,
            -> PaymentStatusEnum.SUCCESS

            StatusEnum.NEW -> PaymentStatusEnum.NEW
            else -> throw InnerException(getTraceId(), "Unknown status for transform to payment status")
        }

    private fun AkbPaymentStatusEnum.toPaymentStatusesEnum(): PaymentStatusEnum =
        when (this) {
            AkbPaymentStatusEnum.PREPARING,
            AkbPaymentStatusEnum.WAITPUSHTRAN,
            AkbPaymentStatusEnum.AUTHORIZED,
            -> PaymentStatusEnum.WAIT

            AkbPaymentStatusEnum.PARTPAID,
            AkbPaymentStatusEnum.REFUNDED,
            AkbPaymentStatusEnum.VOIDED,
            -> PaymentStatusEnum.REFUND

            AkbPaymentStatusEnum.DECLINED,
            AkbPaymentStatusEnum.EXPIRED,
            AkbPaymentStatusEnum.CLOSED,
            -> PaymentStatusEnum.FAIL

            AkbPaymentStatusEnum.REFUSED -> PaymentStatusEnum.DECLINED
            AkbPaymentStatusEnum.FULLYPAID -> PaymentStatusEnum.SUCCESS
        }
}
