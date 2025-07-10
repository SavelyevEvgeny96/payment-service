package ru.sogaz.site.paymentService.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    @Value("\${spring.rabbitmq.queue.name}")
    private lateinit var queueName: String

    @Value("\${spring.rabbitmq.template.exchange}")
    private lateinit var exchange: String

    @Value("\${spring.rabbitmq.template.routing-key}")
    private lateinit var routingKey: String

    @Bean
    fun queue(): Queue = Queue(queueName)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(exchange)

    @Bean
    fun binding(
        queue: Queue,
        exchange: TopicExchange,
    ): Binding =
        BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(routingKey)

    @Bean
    fun jsonMessageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()
}
