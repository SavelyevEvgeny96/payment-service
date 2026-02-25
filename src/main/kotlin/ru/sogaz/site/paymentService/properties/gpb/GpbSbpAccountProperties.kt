package ru.sogaz.site.paymentService.properties.gpb

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbSbpAccountData

@ConfigurationProperties(prefix = "api.gpb.sbp")
class GpbSbpAccountProperties {
    lateinit var account: GpbSbpAccountData
}
