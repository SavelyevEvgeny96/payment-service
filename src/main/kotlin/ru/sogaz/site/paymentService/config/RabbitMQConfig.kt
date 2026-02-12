package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.BindingBuilder.bind
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.config.converters.NoOpMessageConverter
import ru.sogaz.site.paymentService.properties.rabbit.RabbitListenerProps
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties
import org.springframework.context.annotation.Primary
import ru.sogaz.site.paymentService.properties.RabbitProperties

@Configuration
class RabbitMQConfig(
    private val props: RabbitProperties,
) {
    companion object {
        const val QUEUE_TYPE = "x-queue-type"
        const val QUORUM = "quorum"
    }

    @Bean(name = ["paymentsExchange"])
    fun paymentsExchange(): TopicExchange = TopicExchange(props.exchangePayment, true, false)

    @Bean(name = ["ordersExchange"])
    fun ordersExchange(): TopicExchange = TopicExchange(props.exchangeOrder, true, false)

    @Bean(name = ["orderPaidStatusQueue"])
    fun orderPaidStatusQueue(): Queue =
        QueueBuilder
            .durable(props.orderPaidStatusQueue)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean(name = ["paymentsQueueDlq"])
    fun ordersRefundDlq(): Queue =
        QueueBuilder
            .durable("${props.paymentCreatedQueue}.dlq")
            .build()

    @Bean(name = ["paymentsQueue"])
    fun paymentsQueue(): Queue =
        QueueBuilder
            .durable(props.paymentCreatedQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "${props.paymentCreatedQueue}.dlq")
            .build()

    @Bean
    fun paymentsBinding(
        @Qualifier("paymentsQueue") queue: Queue,
        @Qualifier("paymentsExchange") exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(props.routingKeyPaymentCreated)

    @Bean(name = ["paymentsStatusQueue"])
    fun paymentsStatusQueue(): Queue =
        QueueBuilder
            .durable(props.paymentStatusQueue)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean
    fun paymentsStatusBinding(
        @Qualifier("paymentsStatusQueue") queue: Queue,
        @Qualifier("paymentsExchange") exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusPayment)

    @Bean
    fun orderStatusPaidBinding(
        @Qualifier("orderPaidStatusQueue") queue: Queue,
        @Qualifier("ordersExchange") exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusOrderPaid)

    @Bean
    @Primary
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)
}
