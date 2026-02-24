package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData

@ConfigurationProperties(prefix = "api.gpb.card.account")
class GpbCardAccountProperties {
    lateinit var main: GpbCardAccountData
    lateinit var depersonalized: GpbCardAccountData
}
