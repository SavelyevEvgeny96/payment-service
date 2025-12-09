package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.request.PaidOrderMessage
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.payment.PaymentStatusServiceImpl.Companion.START_LOG_MESSAGE_QUEUE
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class SendMessageProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val props: RabbitProperties,
) : SendMessageProducer {
    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)
    override fun sendMessagePaidOrderAndPaymentStatus(
        routingKey: String,
        paidOrderMessage: PaidOrderMessage,
        exchange: String
    ) {
        val timestamp =
            OffsetDateTime
                .now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        logger.info(
            START_LOG_MESSAGE_QUEUE.format(
                routingKey,
                exchange
            )
        )
        rabbitTemplate.convertAndSend(
            exchange,
            routingKey,
            paidOrderMessage,
        ) { message ->
            message.messageProperties.headers["author"] = "payService"
            message.messageProperties.headers["flowCode"] = "ResultPay"
            message.messageProperties.headers["timestamp"] = timestamp
            message
        }
    }
}