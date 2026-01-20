package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProperties {
    private val logger = loggerFor(javaClass)
    lateinit var exchangeOrder: String
    lateinit var exchangePayment: String
    lateinit var paymentStatusQueue: String
    lateinit var paymentRefundQueue: String
    lateinit var paymentCreatedQueue: String
    lateinit var orderPaidStatusQueue: String
    lateinit var routingKeyStatusPayment: String
    lateinit var routingKeyStatusOrderPaid: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchangeOrder = $exchangeOrder")
        logger.info("exchangePayment = $exchangePayment")
        logger.info("paymentStatusQueue = $paymentStatusQueue")
        logger.info("paymentRefundQueue = $paymentRefundQueue")
        logger.info("paymentCreatedQueue = $paymentCreatedQueue")
        logger.info("routingKeyStatusPayment = $routingKeyStatusPayment")
        logger.info("orderPaidStatusQueue = $orderPaidStatusQueue")
        logger.info("routingKeyStatusOrderPaid = $routingKeyStatusOrderPaid")
    }
}
