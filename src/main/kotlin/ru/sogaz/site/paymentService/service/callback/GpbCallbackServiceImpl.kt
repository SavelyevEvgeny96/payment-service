package ru.sogaz.site.paymentService.service.callback

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.mapper.order.SubOrderMapper
import ru.sogaz.site.paymentService.mapper.payment.BankPaymentDetailsMapper
import ru.sogaz.site.paymentService.mapper.payment.RegisterCardMapper
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.metrics.GpbCallbackMetricServiceImpl
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.Instant

@Service
class GpbCallbackServiceImpl(
    private val paymentDao: PaymentDao,
    private val callbackPaymentDao: CallbackPaymentDao,
    private val orderMapper: OrderMapper,
    private val subOrderMapper: SubOrderMapper,
    private val signatureVerifier: SignatureVerifier,
    private val registerCardMapper: RegisterCardMapper,
    private val bankPaymentDetailsMapper: BankPaymentDetailsMapper,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
    private val gpbCallbackMetricService: GpbCallbackMetricServiceImpl,
) : GpbCallbackService {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val LOG_QUEUE_MESSAGE_SENT = "Отправлено в очередь %s TraceId: %s"
        const val START_LOG_MESSAGE_QUEUE = "Старт записи в очередь routingKey: %s  exchange: %s "
        private const val LOG_QUEUE_MESSAGE_ERROR = "Отправка в очередь не удалась: "
        private const val REG_CARD_QUEUE_AUTHOR = "order-service"
        private const val MESSAGE_ROUTING_KEY_IS_NULL = "Отсутствует значение для Routing key"
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        const val INVALID_SIGNATURE = "Invalid signature"
        const val NOT_FOUND = "Not Found"
        const val ERROR_TRX_ID = "Произошла ошибка сертификата для trx_id: "
        const val START_METHOD_PROCESS_CALL =
            ">>> СТАРТ метода проверки CALLBACK от банка" +
                " traceID: "

        const val UPDATE_PAYMENT_STATUS = "Статус платежа в таблице ПЛАТЕЖЕЙ обновлен. paymentBankId: "
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun processCallback(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String> {
        return try {
            val traceId = getTraceId()
            logger.debug(START_METHOD_PROCESS_CALL + traceId)

            if (!signatureVerifier.verifySignature(gpbCallback, httpServletRequest)) {
                logger.debug(ERROR_TRX_ID + gpbCallback.trx_id)
                return createErrorResponse(INVALID_SIGNATURE)
            }

            gpbCallbackMetricService.setMetric(gpbCallback)
            val bankPaymentDetails = bankPaymentDetailsMapper.convert(gpbCallback)

            val payment = paymentDao.findByPaymentBankId(bankPaymentDetails.id)

            if (payment.isClosed() || bankPaymentDetails.status != PaymentStatusEnum.SUCCESS) {
                return createSuccessResponse()
            }

            if (payment.order.skipSendingQueue != true) {
                when (payment.order.regCard) {
                    true -> sendToRegCardQueue(payment.order, bankPaymentDetails)
                    else -> sendToPaidOrdersQueue(payment, bankPaymentDetails)
                }
            }

            callbackPaymentDao.saveCallbackForPayment(payment)

            updatePaymentStatus(payment)
            logger.debug(UPDATE_PAYMENT_STATUS)

            createSuccessResponse()
        } catch (e: InnerException) {
            logger.error(ERROR_TRX_ID + gpbCallback.trx_id)
            createErrorResponse(NOT_FOUND)
        } catch (e: Exception) {
            logger.error(ERROR_TRX_ID + gpbCallback.trx_id)
            createErrorResponse(INTERNAL_SERVER_ERROR)
        }
    }

    private fun updatePaymentStatus(payment: Payment) {
        payment
            .apply { state = PaymentStatusEnum.CALLBACK }
            .run { paymentDao.save(this) }
    }

    private fun sendToPaidOrdersQueue(
        payment: Payment,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        try {
            val subOrders = payment.order.subOrders
            val subOrderPayloads = subOrders.map(subOrderMapper::toSubOrderPayload)

            val requestBody = orderMapper.toPaidOrderMessage(payment.order, subOrderPayloads, bankPaymentDetails)
            requestBody.bank = payment.bank?.name
            val exchange = props.exchangePayment
            val routingKey =
                payment.order.queueStatusResultName
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

            logger.debug(LOG_QUEUE_MESSAGE_SENT.format(payment.order.id, getTraceId()))
        } catch (e: Exception) {
            throw InnerException(getTraceId(), LOG_QUEUE_MESSAGE_ERROR + e.message)
        }
    }

    private fun sendToRegCardQueue(
        order: Order,
        bankPaymentDetails: BankPaymentDetails,
    ) {
        try {
            val channel = order.subOrders.firstOrNull()?.channel ?: ""
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
