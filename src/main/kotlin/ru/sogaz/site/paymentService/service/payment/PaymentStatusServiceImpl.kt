package ru.sogaz.site.paymentService.service.payment

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
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
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.mapper.order.SubOrderMapper
import ru.sogaz.site.paymentService.mapper.payment.PaymentBankInfoMapper
import ru.sogaz.site.paymentService.mapper.payment.RegisterCardMapper
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties
import ru.sogaz.site.paymentService.service.PaymentStatusService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.Instant
import java.time.LocalDateTime

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
    private val sendMessageProducer: SendMessageProducer,
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
    ): Payment {
        handlePaymentChangedStatus(payment, bankPaymentDetails)
        payment.apply {
            state = bankPaymentDetails.status
        }
        return paymentDao.save(payment)
    }

    private fun handlePaymentChangedStatus(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        when {
            bankPaymentDetails.isSuccess() ||
                (bankPaymentDetails.isFail() && payment.order.recurrent == true) ->
                handleSuccessOrFailRecurrentPayment(
                    payment,
                    bankPaymentDetails,
                )
            bankPaymentDetails.isInProcess() -> updateWaitingPaymentsInQueue(bankPaymentDetails.id)
            else -> deleteWaitingPaymentsFromQueue(bankPaymentDetails.id)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    private fun handleSuccessOrFailRecurrentPayment(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
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

        if (payment.state != PaymentStatusEnum.CALLBACK && order.skipSendingQueue != true) {
            when (order.regCard) {
                true -> sendToRegCardQueue(order, bankPaymentDetails)
                else -> sendToPaidOrdersQueue(payment, order, bankPaymentDetails)
            }
        }

        payment.apply {
            state = bankPaymentDetails.status
            paymentFinished = LocalDateTime.now()
        }

        if (bankPaymentDetails.isFail()) {
            order.status = OrderStatus.CANCELED
        } else if (bankPaymentDetails.isSuccess()) {
            order.status = OrderStatus.SUCCESS
        }

        if (order.skipSendingReceipt != true) {
            receiptService.generateReceipt(payment)
        }

        orderDao.save(order)
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

            val requestBody = orderMapper.toPaidOrderMessage(order, subOrderPayloads, bankPaymentDetails)
            requestBody.bank = payment.bank?.name
            val exchange = props.exchangePayment
            val routingKey =
                order.queueStatusResultName
                    ?.takeIf { it.isNotBlank() }
                    ?: props.routingKeyStatusPayment
            logger.debug(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
            logger.debug("Message request queue status: $requestBody")
            val isOrderingClientWithError =
                requestBody.externalSystemCode?.contains("ordering-client") == true &&
                    requestBody.status == StatusEnum.ERROR.value
            if (!isOrderingClientWithError) {
                sendMessageProducer.sendMessage(routingKey, requestBody, exchange, requestBody.orderId)
            }

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

            val exchange = props.exchangeOrder

            logger.debug(START_LOG_MESSAGE_QUEUE.format(routingKey, exchange))
            logger.debug("Message request queue status: $messageBody")
            sendMessageProducer.sendMessage(routingKey, messageBody, exchange, order.id.toString())
            logger.debug(LOG_QUEUE_MESSAGE_SENT.format(order.id, getTraceId()))
        } catch (e: Exception) {
            throw InnerException(getTraceId(), LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }

    fun BankPaymentDetails.isInProcess(): Boolean = status.isInProcess()

    fun BankPaymentDetails.isClosed(): Boolean = status.isClosed()

    fun BankPaymentDetails.isSuccess(): Boolean = status.isSuccess()

    fun BankPaymentDetails.isFail(): Boolean = status.isFail()
}
