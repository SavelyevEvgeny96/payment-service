package ru.sogaz.site.paymentService.properties.rabbit

data class RabbitConcurrencyData(
    val consumers: Int,
    val maxConsumers: Int,
    val stopConsumerMinIntervalMs: Long,
)
