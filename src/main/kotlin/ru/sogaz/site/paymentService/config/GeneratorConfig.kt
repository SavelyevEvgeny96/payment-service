package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.ConfigDataService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GeneratorServiceImpl

@Configuration
class GeneratorConfig {
    @Bean
    fun generatorServiceConfig(configDataService: ConfigDataService): GeneratorService =
        GeneratorServiceImpl(configDataService = configDataService)
}
