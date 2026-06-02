package ru.sogaz.site.paymentService.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService

@Component
@ConditionalOnProperty(name = ["api.version"], havingValue = "v2")
class RecurrentPayConsumer(
    private val payOperationService: PayOperationService,
) {
    companion object {
        private const val RECURRENT_PAY_RESULT = "Результат проведенного по заказу [{}] рекуррентного платежа: {}"
        private const val RECURRENT_PAY_EXCEPTION = "Во время проведения рекуррентного платежа произошла ошибка: {}"
    }

    val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-created-queue}"],
        containerFactory = "concurrentContainerFactory",
    )
    fun recurrentPay(
        @Payload cardRecurrentOperationRequest: CardRecurrentOperationRequest,
    ) {
        try {
            val recurrentOperationDetails = payOperationService.recurrentOperation(cardRecurrentOperationRequest)
            logger.debug(RECURRENT_PAY_RESULT, cardRecurrentOperationRequest.orderId, recurrentOperationDetails.state)
        } catch (ex: Exception) {
            logger.error(RECURRENT_PAY_EXCEPTION, ex.message, ex)
        }
    }
}
