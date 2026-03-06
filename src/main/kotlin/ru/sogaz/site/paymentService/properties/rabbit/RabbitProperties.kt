package ru.sogaz.site.paymentService.properties.rabbit

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

@ConfigurationProperties(prefix = "app.rabbit")
class RabbitProperties {
    private val logger = loggerFor(javaClass)
    lateinit var exchangeOrder: String
    lateinit var exchangePayment: String
    lateinit var exchangeCompletedPayment: String
    lateinit var exchangeCheckStatusPayment: String
    lateinit var paymentStatusQueue: String
    lateinit var paymentRefundQueue: String
    lateinit var paymentCreatedQueue: String
    lateinit var orderPaidStatusQueue: String
    lateinit var routingKeyStatusPayment: String
    lateinit var routingKeyPaymentStatusRefund: String
    lateinit var routingKeyPaymentCreated: String
    lateinit var routingKeyCheckStatusPayment: String
    lateinit var routingKeyStatusOrderPaid: String
    lateinit var routingKeyDetailsPaymentPrefix: String
    lateinit var concurrency: RabbitConcurrencyData

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("exchangeOrder = $exchangeOrder")
        logger.info("exchangePayment = $exchangePayment")
        logger.info("exchangeCompletedPayment = $exchangeCompletedPayment")
        logger.info("exchangeCheckStatusPayment = $exchangeCheckStatusPayment")
        logger.info("paymentStatusQueue = $paymentStatusQueue")
        logger.info("paymentRefundQueue = $paymentRefundQueue")
        logger.info("paymentCreatedQueue = $paymentCreatedQueue")
        logger.info("routingKeyPaymentCreated = $routingKeyPaymentCreated")
        logger.info("routingKeyStatusPayment = $routingKeyStatusPayment")
        logger.info("routingKeyCheckStatusPayment = $routingKeyCheckStatusPayment")
        logger.info("orderPaidStatusQueue = $orderPaidStatusQueue")
        logger.info("routingKeyStatusOrderPaid = $routingKeyStatusOrderPaid")
        logger.info("routingKeyDetailsPaymentPrefix = $routingKeyDetailsPaymentPrefix")
        logger.info("routingKeyPaymentStatusRefund = $routingKeyPaymentStatusRefund")
    }
}
