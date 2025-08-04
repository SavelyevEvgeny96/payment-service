package ru.sogaz.site.paymentService.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "spring.rabbitmq")
class RabbitProperties {
    lateinit var host: String
    var port: Int = 5672
    lateinit var username: String
    lateinit var password: String

    @NestedConfigurationProperty
    lateinit var template: Template

    @NestedConfigurationProperty
    lateinit var queue: QueueConfig

    class Template {
        lateinit var routingKey: String
    }

    class QueueConfig {
        lateinit var name: String
    }
}
