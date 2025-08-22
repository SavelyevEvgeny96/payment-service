package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.site.paymentService.service.impl.AkbBankIntegrationServiceImpl

@Configuration
class AkbBankIntegrationServiceConfig {
    fun configAkbBankIntegrationService(): AkbBankIntegrationService = AkbBankIntegrationServiceImpl()
}