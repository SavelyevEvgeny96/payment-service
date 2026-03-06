package ru.sogaz.site.paymentService.config.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.sogaz.site.paymentService.properties.rabbit.RabbitProperties

@Configuration
class RabbitConfig(
    private val rabbitProperties: RabbitProperties,
) {
    @Bean
    @Primary
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun concurrentContainerFactory(
        connectionFactory: ConnectionFactory,
        jacksonMessageConverter: MessageConverter,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(jacksonMessageConverter)
            setChannelTransacted(true)
            setConcurrentConsumers(rabbitProperties.concurrency.consumers)
            setMaxConcurrentConsumers(rabbitProperties.concurrency.maxConsumers)
            setStopConsumerMinInterval(rabbitProperties.concurrency.stopConsumerMinIntervalMs)
        }
}
