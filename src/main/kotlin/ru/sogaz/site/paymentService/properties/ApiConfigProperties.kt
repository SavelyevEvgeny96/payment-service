package ru.sogaz.site.paymentService.properties

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.loggerFor

/**
 * Класса конфиг.параметров для API платежей и Газпромбанка
 */
@ConfigurationProperties(prefix = "api.payment")
class ApiConfigProperties {
    private val logger = loggerFor(javaClass)
    lateinit var paymentUrl: String
    lateinit var gpbUrl: String
    lateinit var paymentAccount: String
    lateinit var merchantIdSbpGpb: String
    lateinit var callbackUrlSbp: String
    lateinit var gpbSbpUrl: String
    lateinit var portalId: String
    lateinit var merchantId: String
    lateinit var backUrlF: String
    lateinit var backUrlS: String
    lateinit var akbUrl: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentAccount = $paymentAccount")
        logger.info("merchantIdSbpGpb = $merchantIdSbpGpb")
        logger.info("gpbSbpUrl = $gpbSbpUrl")
        logger.info("paymentUrl = $paymentUrl")
        logger.info("gpbUrl = $gpbUrl")
        logger.info("portalId = $portalId")
        logger.info("merchantId = $merchantId")
        logger.info("backUrlF = $backUrlF")
        logger.info("backUrlS = $backUrlS")
        logger.info("akbUrl = $akbUrl")
    }
}
