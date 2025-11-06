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
    lateinit var gpbSbpUrlStatus: String
    lateinit var mainPortalId: String
    lateinit var mainMerchantId: String
    lateinit var depersonalizedPortalId: String
    lateinit var depersonalizedMerchantId: String
    lateinit var backUrlF: String
    lateinit var backUrlS: String
    lateinit var akbUrl: String
    lateinit var akbSbpUrl: String
    lateinit var gpbSbpQRImageUrl: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("paymentAccount = $paymentAccount")
        logger.info("merchantIdSbpGpb = $merchantIdSbpGpb")
        logger.info("gpbSbpUrl = $gpbSbpUrl")
        logger.info("paymentUrl = $paymentUrl")
        logger.info("gpbUrl = $gpbUrl")
        logger.info("mainPortalId = $mainPortalId")
        logger.info("mainMerchantId = $mainMerchantId")
        logger.info("depersonalizedPortalId = $depersonalizedPortalId")
        logger.info("depersonalizedMerchantId = $depersonalizedMerchantId")
        logger.info("backUrlF = $backUrlF")
        logger.info("backUrlS = $backUrlS")
        logger.info("akbSbpUrl = $akbSbpUrl")
        logger.info("akbUrl = $akbUrl")
        logger.info("gpbSbpQRImageUrl = $gpbSbpQRImageUrl")
    }
}
