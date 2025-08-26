package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl

@Configuration
class GazpromServiceConfig {
    @Bean
    fun daoConfig(
        apiConfigProperty: ApiConfigProperties,
        objectMapper: ObjectMapper,
        restTemplate: WebConfigRestTemplate,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
        subOrderDao: SubOrderDao,
        paymentDao: PaymentDao,
        generatorService: GeneratorService,
    ): GazpromService =
        GazpromServiceImpl(
            apiConfigProperty = apiConfigProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            paymentDao = paymentDao,
            generatorService = generatorService,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
            subOrderDao = subOrderDao
        )
}
