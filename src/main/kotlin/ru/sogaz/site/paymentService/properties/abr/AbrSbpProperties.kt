package ru.sogaz.site.paymentService.properties.abr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.abr.sbp")
class AbrSbpProperties {
    lateinit var redirectUrl: String
}
