package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.impl.ConfigDataDaoImpl
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository

@Configuration
class DataDaoConfig {
    @Bean
    fun daoConfig(
        apiConfigProperty: ApiConfigProperty,
        configDataRepository: ConfigDataRepository,
        objectMapper: ObjectMapper,
        restTemplate: WebConfigRestTemplate,
        bankRepository: BankRepository

    ): ConfigDataDao =
        ConfigDataDaoImpl(
            configDataRepository = configDataRepository,
            apiConfigProperty = apiConfigProperty,
            restTemplate =restTemplate ,
            objectMapper =objectMapper ,
            bankRepository = bankRepository
        )
}
