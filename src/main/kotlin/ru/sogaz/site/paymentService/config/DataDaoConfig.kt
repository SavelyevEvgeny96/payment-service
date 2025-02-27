package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.impl.ConfigDataDaoImpl
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

@Configuration
class DataDaoConfig {
    @Bean
    open fun dataDaoConfig(
        configDataRepository: ConfigDataRepository
    ): ConfigDataDao =
        ConfigDataDaoImpl(
            configDataRepository = configDataRepository
        )
}