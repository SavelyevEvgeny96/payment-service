package ru.sogaz.site.paymentService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.receipt")
class ReceiptProperties {
    lateinit var receiptUrl: String
}
