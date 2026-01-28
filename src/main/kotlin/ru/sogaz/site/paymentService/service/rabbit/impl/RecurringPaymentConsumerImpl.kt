package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dto.data.ParsedResult
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.PaidOrderMessageMapper
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.rabbit.BuildBatchConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RecurringPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer

@Service
class RecurringPaymentConsumerImpl(
    private val buildBatchConsumerService: BuildBatchConsumerService,
    private val paidOrderMessageMapper: PaidOrderMessageMapper,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
) : RecurringPaymentConsumer {
    companion object {

        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED =
            "Нет валидных сообщений для обработки"
    }

    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-created-queue}"],
    )
    override fun handleBatch(
        messages: Message,
        channel: Channel,
    ) {
        val parsedResults = sendMessageProducer.parseBatch(messages, channel, OrderPayloadDto::class.java)
        if (parsedResults == null) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            return
        }
        if (parsedResults is ParsedResult.Success) {
            val dtoSuccess = parsedResults.dto
            //  Бизнес-логика (БЕЗ ACK)
            val processSinglePayloadResponse =
                buildBatchConsumerService.processSinglePayload(dtoSuccess)
            paidOrderMessageMapper
                .toPaidOrderMessage(processSinglePayloadResponse)
                .let { message ->
                    if (message.status?.contains("error") == true) {
                        processSinglePayloadResponse.payment.order.queueStatusResultName
                            ?.takeIf { it.isNotBlank() }
                            ?.also { routingKey ->
                                if (message.externalSystemCode?.contains("ordering-client") == false) {
                                    sendMessageProducer.sendMessage(
                                        routingKey,
                                        message,
                                        props.exchangePayment,
                                        message.orderId,
                                    )
                                }
                                sendMessageProducer.sendMessage(
                                    props.routingKeyStatusOrderPaid,
                                    message,
                                    props.exchangeOrder,
                                    message.orderId,
                                )
                            }
                    }
                }
        }
    }
}
