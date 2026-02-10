package ru.sogaz.site.paymentService.properties.rabbit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rabbit.listener")
data class RabbitListenerProps(
    var batchSize: Int?,
    var prefetch: Int?,
    var concurrency: Int?,
    var maxConcurrency: Int?,
)
