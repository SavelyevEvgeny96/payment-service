package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

/**
 * Класса конфиг.параметров для API платежей и Газпромбанка
 */
@ConfigurationProperties(prefix = "api.payment")
class ApiConfigProperty {
    private val logger = loggerFor(javaClass)

    lateinit var paymentUrl: String
    lateinit var gpbUrl: String
    lateinit var portalId: String
    lateinit var merchantId: String
    lateinit var backUrlF: String
    lateinit var backUrlS: String
            @PostConstruct

    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentUrl = $paymentUrl")
        logger.info("gpbUrl = $gpbUrl")
        logger.info("portalId = $portalId")
        logger.info("merchantId = $merchantId")
        logger.info("backUrlF = $backUrlF")
        logger.info("backUrlS = $backUrlS")
    }
}