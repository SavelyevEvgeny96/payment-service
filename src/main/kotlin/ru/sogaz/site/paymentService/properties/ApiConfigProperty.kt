package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

/**
 * Класса конфиг.параметров
 */
@ConfigurationProperties(prefix = "api.payment")
class ApiConfigProperty {
    private val logger = loggerFor(javaClass)
    lateinit var paymentUrl: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentUrl = " + paymentUrl)
    }
}
