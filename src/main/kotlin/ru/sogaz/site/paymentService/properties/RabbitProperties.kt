package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProperties {
    private val logger = loggerFor(javaClass)
    lateinit var exchange: String
    lateinit var queueStatusPayment: String
    lateinit var routingKeyStatusPayment: String
    lateinit var paymentCreateQueue: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchange = $exchange")
        logger.info("queueStatusPayment = $queueStatusPayment")
        logger.info("routingKeyStatusPayment = $routingKeyStatusPayment")
        logger.info("paymentCreateQueue = $paymentCreateQueue")
    }
}
