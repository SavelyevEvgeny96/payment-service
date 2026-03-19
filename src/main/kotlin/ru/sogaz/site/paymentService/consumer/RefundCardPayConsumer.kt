package ru.sogaz.site.paymentService.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.operation.RefundOperationRequestMapper
import ru.sogaz.site.paymentService.model.v2.event.RefundEvent
import ru.sogaz.site.paymentService.service.v2.pay.RefundPayOperationService

@Component
@ConditionalOnProperty(name = ["api.version"], havingValue = "v2")
class RefundCardPayConsumer(
    private val idempotentOrderOperationDao: IdempotentOrderOperationDao,
    private val refundOperationRequestMapper: RefundOperationRequestMapper,
    private val refundPayOperationService: RefundPayOperationService,
) {
    companion object {
        private const val REFUND_RESULT = "Результат проведенного по заказу [{}] возврата: {}"
        private const val REFUND_EXCEPTION = "Во время проведения возврата произошла ошибка: {}"
    }

    val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-refund-queue}"],
        containerFactory = "concurrentContainerFactory",
    )
    fun recurrentPay(
        @Payload refundEvent: RefundEvent,
    ) {
        try {
            val operation = idempotentOrderOperationDao.findSucceededByOrderId(refundEvent.orderId) ?: throw Exception()
            val refundOperationRequest = refundOperationRequestMapper.toRefundOperationRequest(operation, refundEvent)
            val recurrentOperationDetails = refundPayOperationService.refundPayOperation(refundOperationRequest)
            logger.debug(REFUND_RESULT, refundEvent.orderId, recurrentOperationDetails.state)
        } catch (ex: Exception) {
            logger.error(REFUND_EXCEPTION, ex.message, ex)
        }
    }
}
