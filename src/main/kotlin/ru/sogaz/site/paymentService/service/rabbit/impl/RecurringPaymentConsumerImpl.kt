package ru.sogaz.site.paymentService.service.rabbit.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dto.data.TaggedPayload
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
        private const val BATCH_SUMMARY =
            "Итог обработки пачки: количество=%d, длительность(мс)=%d"
    }

    private val logger = loggerFor(RecurringPaymentConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-created-queue}"],
        containerFactory = "batchContainerFactory",
    )
    override fun handleBatch(
        messages: List<Message>,
        channel: Channel,
    ) {

        val started = System.nanoTime()
        // 1) Парсим сообщения → оставляем только валидные
        val payloads = messages.mapNotNull { sendMessageProducer.toTaggedPayload(it, OrderPayloadDto::class.java) }
        if (payloads.isEmpty()) {
            logger.warn("Нет валидных сообщений для обработки в батче")
            return
        }

        // 2) Обрабатываем батч в сервисе
        val batchResult =
            runCatching {
                buildBatchConsumerService.upsertBatch(payloads, channel) // List<PaymentRecurrentRegisterData>
            }.onFailure { ex ->
                logger.error("Ошибка при обработке батча: ${ex.message}", ex)
            }.getOrNull() ?: return

        // 3) Мапим и шлём каждое сообщение с его routingKey
        batchResult.forEach { regData ->
            paidOrderMessageMapper
                .toPaidOrderMessage(regData)
                .let { message ->
                    if (message.status?.contains("error") == true) {
                        regData.payment.order.queueStatusResultName
                            ?.takeIf { it.isNotBlank() }
                            ?.also { routingKey ->
                                logger.info(
                                    "Отправляем PaidOrderMessage для paymentId=${regData.payment.id}, " +
                                            "routingKey=$routingKey",
                                ) // отправляем в очередь для внешних систем
                                // Но если это ordering-client не отправляем ошибку
                                if (message.externalSystemCode?.contains("ordering-client") == false) {
                                    sendMessageProducer.sendMessage(
                                        routingKey,
                                        message,
                                        props.exchangePayment,
                                        message.orderId
                                    )
                                } // отправляем в очередь для ordering-service
                                sendMessageProducer.sendMessage(
                                    props.routingKeyStatusOrderPaid,
                                    message,
                                    props.exchangeOrder,
                                    message.orderId
                                )
                                logger.info(
                                    "Отправляем PaidOrderMessage для paymentId=${regData.payment.id}, " +
                                            "routingKey=${props.routingKeyStatusOrderPaid}",
                                )
                            }
                    } else {
                        // success  сделать тогда  когда чеки переедут в ордеринг тогда уберем условие .contains("error")
                    }
                }
        }

        val tookMs = (System.nanoTime() - started) / 1_000_000
        logger.info(BATCH_SUMMARY.format(payloads.size, tookMs))
    }



}
