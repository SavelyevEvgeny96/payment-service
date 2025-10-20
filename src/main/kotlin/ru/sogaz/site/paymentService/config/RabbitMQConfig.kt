package ru.sogaz.site.paymentService.config

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.RabbitProperties

/** начало генерации ИИ qwen2.5-coder:14b  */

@Configuration
class RabbitMQConfig(
    private val rabbit: RabbitProperties,
) {
    private var queueName = rabbit.template.routingKey

    @Bean
    fun queue(): Queue = Queue(queueName)
}
/** Конец генерации ИИ qwen2.5-coder:14b  */
