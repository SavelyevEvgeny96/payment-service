package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder.bind
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.RabbitProperties

@Configuration
class RabbitMQConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProperties,
) {
    @Bean
    fun ordersExchange(): TopicExchange = TopicExchange(props.exchange, true, false)

    @Bean(name = ["paymentsStatusQueue"])
    fun paymentsStatusQueue(): Queue =
        QueueBuilder
            .durable(props.queueStatusPayment)
            .withArgument("x-queue-type", "quorum")
            .build()

    @Bean
    fun ordersBinding(
        @Qualifier("paymentsStatusQueue") queue: Queue,
        exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusPayment)

    @Bean
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun rabbitTemplate(messageConverter: MessageConverter): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
}
