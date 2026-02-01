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
import ru.sogaz.site.paymentService.service.rabbit.BuildConsumerService
import ru.sogaz.site.paymentService.service.rabbit.RecurringPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer

/**
 * Консьюмер для обработки сообщений о создании платежей
 * по рекуррентным заказам.
 *
 * Получает сообщения из RabbitMQ, парсит сообщение,
 * обрабатывает каждое сообщение бизнес-логикой
 * и отправляет статусные события в соответствующие очереди.
 *
 * ACK отправляется только после успешной обработки.
 * В случае ошибки выполняется REJECT без повторной постановки в очередь.
 */
@Service
class RecurringPaymentConsumerImpl(
    private val buildConsumerService: BuildConsumerService,
    private val paidOrderMessageMapper: PaidOrderMessageMapper,
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
) : RecurringPaymentConsumer {

    companion object {
        /** Сообщение логирования при отсутствии валидных сообщений в батче */
        private const val NOT_VALID_BATCH_MESSAGE_ORDER_CREATED =
            "Нет валидных сообщений для обработки"
    }

    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)

    /**
     * Обработчик сообщений из очереди payment.created.queue
     *
     * @param messages AMQP-сообщение
     * @param channel канал RabbitMQ для ручного управления ACK/REJECT
     */
    @RabbitListener(
        queues = ["\${app.rabbit.payment-created-queue}"],
    )
    override fun handleMessage(
        messages: Message,
        channel: Channel,
    ) {
        // Парсинг сообщения messages->OrderPayloadDto
        val parsedResults = sendMessageProducer.parseMessage(
            messages,
            channel,
            OrderPayloadDto::class.java
        )

        // Если результат невалидный или пустой — просто логируем и выходим
        if (parsedResults == null) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            return
        }

        // Обрабатываем только успешный результат парсинга
        if (parsedResults is ParsedResult.Success) {
            val dtoSuccess = parsedResults.dto

            try {
                // Основная бизнес-логика обработки payload
                val processSinglePayloadResponse =
                    buildConsumerService.processSinglePayload(dtoSuccess)

                // Маппинг результата в сообщение для внешних систем
                paidOrderMessageMapper
                    .toPaidOrderMessage(processSinglePayloadResponse)
                    .let { message ->

                        // Если статус обработки содержит ошибку —
                        // отправляем статусные сообщения
                        if (message.status?.contains("error") == true) {

                            processSinglePayloadResponse.payment.order.queueStatusResultName
                                ?.takeIf { it.isNotBlank() }
                                ?.also { routingKey ->

                                    // Отправка сообщения во внешнюю систему,
                                    // если это не ordering-client
                                    if (message.externalSystemCode
                                            ?.contains("ordering-client") == false
                                    ) {
                                        sendMessageProducer.sendMessage(
                                            routingKey,
                                            message,
                                            props.exchangePayment,
                                            message.orderId,
                                        )
                                    }

                                    // Отправка статуса в ordering-service
                                    sendMessageProducer.sendMessage(
                                        props.routingKeyStatusOrderPaid,
                                        message,
                                        props.exchangeOrder,
                                        message.orderId,
                                    )
                                }
                        }
                    }

                // ACK сообщения после успешной обработки
                channel.basicAck(parsedResults.tag, true)

            } catch (ex: Exception) {
                // REJECT без requeue при любой ошибке бизнес-логики
                channel.basicReject(parsedResults.tag, false)
                logger.error("Ошибка обработки сообщения", ex)
            }
        }
    }
}
