package ru.sogaz.site.paymentService.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.operation.RefundOperationRequestMapper
import ru.sogaz.site.paymentService.model.v2.event.RefundEvent
import ru.sogaz.site.paymentService.service.v2.pay.RefundPayOperationService
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(name = ["api.version"], havingValue = "v2")
class RefundCardPayConsumer(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val refundOperationRequestMapper: RefundOperationRequestMapper,
    private val refundPayOperationService: RefundPayOperationService,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val REFUND_RESULT =
            "Результат проведенного по заказу [{}] отмены: {}"

        private const val REFUND_EXCEPTION =
            "Во время проведения отмены произошла ошибка: {}"

        private const val REFUND_MESSAGE_PARSE_EXCEPTION =
            "Не удалось распарсить сообщение на отмену платежа. Ошибка: {}. Сообщение: {}"
    }

    val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-reversal-queue}"],
        containerFactory = "concurrentContainerFactory",
    )
    fun reversalPay(
        message: Message
    ) {
        val rawMessage = String(message.body, StandardCharsets.UTF_8)

        val refundEvent = try {
            objectMapper.readValue(rawMessage, RefundEvent::class.java)
        } catch (ex: Exception) {
            logger.error(
                REFUND_MESSAGE_PARSE_EXCEPTION,
                ex.message,
                rawMessage,
                ex,
            )
            return
        }

        try {
            val operation =
                idempotentOrderOperationDao.findSucceededByPaymentBankId(refundEvent.paymentBankId)
                    ?: throw Exception()

            val refundOperationRequest =
                refundOperationRequestMapper.toRefundOperationRequest(operation, refundEvent)

            val recurrentOperationDetails =
                refundPayOperationService.refundPayOperation(refundOperationRequest)

            logger.debug(
                REFUND_RESULT,
                refundEvent.paymentBankId,
                recurrentOperationDetails.state,
            )
        } catch (ex: Exception) {
            logger.error(REFUND_EXCEPTION, ex.message, ex)
        }
    }
}