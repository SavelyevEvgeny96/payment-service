package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.loggingStarter.rabbitLogging.RabbitLogConst
import ru.sogaz.site.paymentService.dto.request.PaidOrderMessage
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.payment.PaymentStatusServiceImpl.Companion.START_LOG_MESSAGE_QUEUE
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
) : SendMessageProducer {
    private val logger = loggerFor(SendMessageProducerImpl::class.java)

    override fun sendMessagePaidOrderAndPaymentStatus(
        routingKey: String,
        paidOrderMessage: PaidOrderMessage,
        exchange: String,
    ) {
        val timestamp =
            OffsetDateTime
                .now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        logger.info(
            START_LOG_MESSAGE_QUEUE.format(
                routingKey,
                exchange,
            ),
        )
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            paidOrderMessage,
        ) { message ->
            message.messageProperties.headers["author"] = "payService"
            message.messageProperties.headers["flowCode"] = "ResultPay"
            message.messageProperties.headers["timestamp"] = timestamp
            message.messageProperties.headers[RabbitLogConst.HDR_X_EXCHANGE] = exchange
            message.messageProperties.headers[RabbitLogConst.HDR_X_ROUTINGKEY] = routingKey
            message.messageProperties.correlationId = paidOrderMessage.orderId.toString()
            message
        }
    }
}
