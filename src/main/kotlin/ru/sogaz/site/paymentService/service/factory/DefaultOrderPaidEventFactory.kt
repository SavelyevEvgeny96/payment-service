package ru.sogaz.site.paymentService.service.factory

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import ru.sogaz.site.paymentService.dto.rabbit.OrderPaidEvent
import ru.sogaz.site.paymentService.enums.OrderPaidStatus
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.OrderPaidEventFactory
import java.time.Instant
import java.util.UUID

@Component
class DefaultOrderPaidEventFactory(
    private val rabbitProperties: RabbitProperties,
) : OrderPaidEventFactory {
    companion object {
        private const val AUTHOR = "payment-receipt-service"
    }

    override fun success(orderId: UUID?): OrderPaidEvent =
        OrderPaidEvent(
            metaInfo = meta(),
            orderId = orderId,
            status = OrderPaidStatus.SUCCESS,
        )

    override fun error(
        orderId: UUID?,
        errorText: String,
    ): OrderPaidEvent =
        OrderPaidEvent(
            metaInfo = meta(),
            orderId = orderId,
            status = OrderPaidStatus.ERROR,
            errorText = errorText,
        )

    private fun meta() =
        MetaInfoOrder(
            eventTimeIso = Instant.now(),
            author = AUTHOR,
            routingKey = rabbitProperties.routingKeyStatusOrderPaid,
        )
}
