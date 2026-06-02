package ru.sogaz.site.paymentService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.gpb")
class GpbConfigProperties {
    lateinit var certs: String
    lateinit var callbackUrl: String
}
