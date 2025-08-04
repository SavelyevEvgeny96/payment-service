package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GeneratorServiceImpl
import ru.sogaz.site.paymentService.util.Util

@Configuration
class GeneratorConfig {
    @Bean
    fun generatorServiceConfig(util: Util): GeneratorService = GeneratorServiceImpl(util = util)
}
