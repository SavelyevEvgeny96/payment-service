package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Настройки интеграции оплаты банковской картой в ГИД через Газпромбанк.
 */
@ConfigurationProperties(prefix = "api.gpb.gid")
class GpbGidProperties {
    lateinit var portalId: String
    lateinit var depersonalizedPortalId: String
    lateinit var merchantId: String
    lateinit var depersonalizedMerchantId: String
    lateinit var accountId: String
    lateinit var depersonalizedAccountId: String
    lateinit var backUrlSuccess: String
    lateinit var backUrlFail: String
}
