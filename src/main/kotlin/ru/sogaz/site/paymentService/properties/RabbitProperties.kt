package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProps {
    private val logger = loggerFor(javaClass)
    var exchange: String = "payments.exchange"
    var queueStatusPayment: String = "payment.status.queue"
    var routingKeyStatusPayment: String = "payment.status.created"


    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchange = $exchange")
        logger.info("queueStatusPayment = $queueStatusPayment")
        logger.info("routingKeyStatusPayment = $routingKeyStatusPayment")

    }
}
