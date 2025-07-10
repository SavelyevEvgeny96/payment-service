package ru.sogaz.site.paymentService.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.receipt")
class ReceiptProperty {
    lateinit var receiptUrl: String
    lateinit var receiptPath: String
}
