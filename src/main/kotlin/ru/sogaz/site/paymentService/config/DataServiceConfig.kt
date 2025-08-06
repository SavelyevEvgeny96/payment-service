package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.ConfigDataService
import ru.sogaz.site.paymentService.service.impl.ConfigDataServiceImpl

@Configuration
class DataServiceConfig {
    @Bean
    fun configDataService(configDataRepository: ConfigDataRepository): ConfigDataService =
        ConfigDataServiceImpl(configDataRepository = configDataRepository)
}
