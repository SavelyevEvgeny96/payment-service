package ru.sogaz.site.paymentService.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration


class GpbIntegrationProperties {
    @Value("\${api.gazprom.card.mainPortalId}")
    lateinit var mainPortalId: String

    @Value("\${api.gazprom.card.depersonalizedPortalId}")
    lateinit var depersonalizedPortalId: String

    @Value("\${api.gazprom.card.mainMerchantId}")
    lateinit var mainMerchantId: String

    @Value("\${api.gazprom.card.depersonalizedMerchantId}")
    lateinit var depersonalizedMerchantId: String
}
