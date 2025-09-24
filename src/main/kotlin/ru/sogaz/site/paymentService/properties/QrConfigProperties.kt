package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

/**
 * Класса конфиг параметров для QR генератор сервиса
 */
@ConfigurationProperties(prefix = "api.qr")
class QrConfigProperties {
    private val logger = loggerFor(javaClass)
    lateinit var baseUrl: String

    @PostConstruct
    fun postConstruct() {
        logger.info("QR generator service path: $baseUrl")
    }
}