package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
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
        subOrderRepository: SubOrderRepository,
        paymentOperationHistoryDao:PaymentOperationHistoryDao,
        paymentRepository: PaymentRepository,
        getActionTypeDao: GetActionTypeDao,
        getPaymentStatusDao: GetPaymentStatusDao,
        paymentDao: PaymentDao,
        generatorService: GeneratorService,
    ): GazpromService =
        GazpromServiceImpl(
            apiConfigProperty = apiConfigProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            subOrderRepository = subOrderRepository,

            paymentRepository = paymentRepository,
            getActionTypeDao = getActionTypeDao,
            getPaymentStatusDao = getPaymentStatusDao,
            paymentDao = paymentDao,
            generatorService = generatorService,
            paymentOperationHistoryDao = paymentOperationHistoryDao
        )
}
