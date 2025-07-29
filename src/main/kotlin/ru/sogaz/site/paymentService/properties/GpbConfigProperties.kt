package ru.sogaz.site.paymentService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.certs")
class GpbConfigProperties {
    lateinit var gpb: String
}
