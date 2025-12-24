package ru.sogaz.site.paymentService.service.rabbit.impl

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dto.rabbit.OrderPaidEvent
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.rabbit.OrderPaidEventProducer

@Service
class OrderPaidEventProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val props: RabbitProperties,
) : OrderPaidEventProducer {
    override fun send(event: OrderPaidEvent) {
        rabbitTemplate.convertAndSend(
            props.exchangePayment,
            props.routingKeyStatusOrderPaid,
            event,
        )
    }
}
