package ru.sogaz.site.paymentService.service.payment

import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.PaymentBankInfo
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.mapper.order.SubOrderMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentBankInfoMapper
import ru.sogaz.site.paymentService.mapper.payment.RegisterCardMapper
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.PaymentStatusService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationFactoryService
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class PaymentStatusServiceImpl(
    private val orderDao: OrderDao,
    private val subOrderDao: SubOrderDao,
    private val paymentDao: PaymentDao,
    private val waitingPaymentDao: WaitingPaymentDao,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val receiptService: ReceiptService,
    private val props: RabbitProperties,
    private val rabbitTemplate: RabbitTemplate,
    private val operationHistoryDao: PaymentOperationHistoryDao,
    private val bankIntegrationFactoryService: BankIntegrationFactoryService,
    private val paymentBankInfoMapper: PaymentBankInfoMapper,
    private val subOrderMapper: SubOrderMapper,
    private val orderMapper: OrderMapper,
    private val registerCardMapper: RegisterCardMapper,
) : PaymentStatusService {
    companion object {
        private const val UPDATE_STATUS_ERROR_MESSAGE =
            "Произошла ошибка во время обновления статуса для банковского платежа [bankId: %s]"
        private const val NOT_FIND_ORDER_WARN_MESSAGE = "Не найден заказ для банковского платежа"
        private const val ORDER_ALREADY_PAID_WARN_MESSAGE =
            "Заказ [orderId: %s, bank: %s] уже " +
                "отмечен как оплаченный для банковского платежа"
        private const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val START_LOG_MESSAGE_QUEUE = "Старт записи в очередь routingKey: %s  exchange: %s "
        private const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        private const val REG_CARD_QUEUE_AUTHOR = "order-service"
        private const val MESSAGE_ROUTING_KEY_IS_NULL = "Отсутствует значение для Routing key"
    }

    private val logger = loggerFor(javaClass)

    override fun updateStatus(callbackPayment: CallbackPayment): Payment? =
        callbackPayment.paymentBankId
            .run(::updateStatus)

    override fun updateStatus(waitingPayment: WaitingPayment): Payment? =
        waitingPayment.paymentBankId
            .run(::updateStatus)

    override fun updateStatus(paymentBankId: String): Payment? =
        try {
            paymentBankId
                .run(paymentDao::findByPaymentBankId)
                .run(::updateStatus)
        } catch (ex: Exception) {
            logger.error(UPDATE_STATUS_ERROR_MESSAGE.format(paymentBankId), ex)
            updateWaitingPaymentsInQueue(paymentBankId)
            null
        }

    private fun updateStatus(payment: Payment): Payment {
        if (payment.isClosed()) {
            deleteWaitingPaymentsFromQueue(payment.paymentBankId!!)
            return payment
        }
        return payment
            .run(::requestCurrentStatusInBank)
            .run { updateStatusesForPayment(this, payment) }
    }

    private fun requestCurrentStatusInBank(payment: Payment): BankPaymentDetails =
        payment
            .run(paymentBankInfoMapper::convert)
            .run(::requestCurrentStatusInBank)

    private fun requestCurrentStatusInBank(paymentBankInfo: PaymentBankInfo): BankPaymentDetails =
        bankIntegrationFactoryService
            .getInstanceByBank(paymentBankInfo.bank)
            .requestPaymentStatus(paymentBankInfo)

    @Transactional(rollbackFor = [Exception::class])
    private fun updateStatusesForPayment(
        bankPaymentDetails: BankPaymentDetails,
        payment: Payment,
    ): Payment =
        payment
            .apply { state = bankPaymentDetails.status }
            .also { handlePaymentChangedStatus(it, bankPaymentDetails) }
            .run(paymentDao::save)

    private fun handlePaymentChangedStatus(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        when {
            payment.isSuccess() -> handleSuccessPayment(payment, bankPaymentDetails)
            payment.isInProcess() -> updateWaitingPaymentsInQueue(bankPaymentDetails.id)
            else -> deleteWaitingPaymentsFromQueue(bankPaymentDetails.id)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun handleSuccessPayment(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        payment.paymentFinished = LocalDateTime.now()
        updateOrderForSuccessPayment(payment, bankPaymentDetails)
        deleteWaitingPaymentsFromQueue(bankPaymentDetails.id)
    }

    private fun updateOrderForSuccessPayment(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        val order = payment.order
        if (order.status == OrderStatus.SUCCESS) {
            logger.warn("${ORDER_ALREADY_PAID_WARN_MESSAGE.format(order.id, payment.bank)} ${payment.paymentBankId}")
            return
        }
        order.apply { status = OrderStatus.SUCCESS }

        order.skipSendingReceipt?.ifFalse { receiptService.generateReceipt(payment) }

        if (order.skipSendingQueue != true) {
            when (order.regCard) {
                true -> sendToRegCardQueue(order, bankPaymentDetails)
                else -> sendToPaidOrdersQueue(payment, order, bankPaymentDetails)
            }
        }

        orderDao.save(order)
        operationHistoryDao.saveForOrder(order, ActionType.ORDER_PAID.value)
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun deleteWaitingPaymentsFromQueue(paymentBankId: String) {
        waitingPaymentDao.deleteByPaymentBankId(paymentBankId)
        callbackPaymentDao.deleteByPaymentBankId(paymentBankId)
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun updateWaitingPaymentsInQueue(paymentBankId: String) {
        waitingPaymentDao.updateTimeByPaymentBankId(paymentBankId)
        callbackPaymentDao.updateTimeByPaymentBankId(paymentBankId)
    }

    private fun sendToPaidOrdersQueue(
        payment: Payment,
        order: Order,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        try {
            val subOrders = subOrderDao.getAllSubOrderListByOrderId(order) ?: emptyList()
            val subOrderPayloads = subOrders.map(subOrderMapper::toSubOrderPayload)

            val requestBody = orderMapper.toPaidOrderMessage(order, subOrderPayloads, bankPaymentDetails.cardDetails)
            requestBody.bank = payment.bank?.name
            val exchange = props.exchangePayment
            val routingKey =
                order.queueStatusResultName
                    ?.takeIf { it.isNotBlank() }
                    ?: props.routingKeyStatusPayment

            val cd = CorrelationData(order.id.toString())
            val timestamp =
                OffsetDateTime
                    .now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            logger.debug(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
            logger.debug("Message request queue status: $requestBody")
            rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                requestBody,
                { message ->
                    message.messageProperties.headers["author"] = "payService"
                    message.messageProperties.headers["flowCode"] = "ResultPay"
                    message.messageProperties.headers["timestamp"] = timestamp
                    message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                    message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                    message.messageProperties.correlationId = order.id.toString()
                    message
                },
                cd,
            )

            logger.debug(LOG_QUEUE_MESSAGE_SENT.format(order.id, getTraceId()))
        } catch (e: Exception) {
            throw InnerException(getTraceId(), LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }

    private fun sendToRegCardQueue(
        order: Order,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        try {
            val channel = subOrderDao.getAllSubOrderListByOrderId(order)?.first()?.channel ?: ""
            val instantTime = Instant.now()
            val routingKey = requireNotNull(order.queueStatusResultName) { MESSAGE_ROUTING_KEY_IS_NULL }

            val metaInfoOrder = MetaInfoOrder(instantTime, REG_CARD_QUEUE_AUTHOR, routingKey)
            val messageBody =
                registerCardMapper.toStatusRegisterCardMessage(
                    metaInfoOrder,
                    order,
                    channel,
                    bankPaymentDetails,
                    instantTime,
                )

            val exchange = props.exchangePayment
            val cd = CorrelationData(order.id.toString())

            logger.debug(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
            logger.debug("Message request queue status: $messageBody")

            rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                messageBody,
                { message ->
                    message.messageProperties.headers["author"] = "payService"
                    message.messageProperties.headers["flowCode"] = "ResultPay"
                    message.messageProperties.headers["timestamp"] =
                        OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                    message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
                    message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
                    message.messageProperties.correlationId = order.id.toString()
                    message
                },
                cd,
            )

            logger.debug(LOG_QUEUE_MESSAGE_SENT.format(order.id, getTraceId()))
        } catch (e: Exception) {
            throw InnerException(getTraceId(), LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }
}
