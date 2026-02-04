package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.gpb.sbp")
class GpbSbpPaymentProperties {
    lateinit var basePath: String
    lateinit var callbackUrlSbp: String
    lateinit var merchantIdSbpGpb: String
    lateinit var paymentAccount: String
}
