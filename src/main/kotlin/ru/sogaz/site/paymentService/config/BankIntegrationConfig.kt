package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.SslClientProperties
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService

@Configuration
class BankIntegrationConfig {
    @Bean
    fun bankIntegrationFactory(
        apiConfigProperties: ApiConfigProperties,
        webConfigRestTemplate: WebConfigRestTemplate,
        props: SslClientProperties,
    ): BankIntegrationFactoryService =
        BankIntegrationFactoryService(
            apiConfigProperties = apiConfigProperties,
            webConfigRestTemplate = webConfigRestTemplate,
            props = props,
        )
}
