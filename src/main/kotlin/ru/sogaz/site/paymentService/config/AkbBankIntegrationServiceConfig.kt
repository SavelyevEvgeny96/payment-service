package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.AkbBankIntegrationServiceImpl

@Configuration
class AkbBankIntegrationServiceConfig {
    @Bean
    fun configAkbBankIntegrationService(
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
        apiConfigProperty: ApiConfigProperties,
        subOrderDao: SubOrderDao,
        generatorService: GeneratorService,
        restTemplate: WebConfigRestTemplate,
        objectMapper: ObjectMapper,
        paymentDao: PaymentDao
    ): AkbBankIntegrationService =
        AkbBankIntegrationServiceImpl(
            paymentOperationHistoryDao = paymentOperationHistoryDao,
            apiConfigProperty = apiConfigProperty,
            generatorService = generatorService,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            paymentDao = paymentDao,
            subOrderDao = subOrderDao
        )
}