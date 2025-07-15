package ru.sogaz.site.paymentService.config

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.RabbitProperties

@Configuration
class RabbitMQConfig(
    private val rabbit: RabbitProperties,
) {
    private var queueName = rabbit.queue.name

    @Bean
    fun queue(): Queue = Queue(queueName)
}
