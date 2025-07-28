package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl
import ru.sogaz.site.paymentService.util.Util

@Configuration
class GazpromServiceConfig {
    @Bean
    fun daoConfig(
        apiConfigProperty: ApiConfigProperty,
        objectMapper: ObjectMapper,
        restTemplate: WebConfigRestTemplate,
        subOrderRepository: SubOrderRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentRepository: PaymentRepository,
        getActionTypeDao: GetActionTypeDao,
        util: Util,
        getPaymentStatusDao: GetPaymentStatusDao,
        getPaymentDao: GetPaymentDao
    ): GazpromService =
        GazpromServiceImpl(
            apiConfigProperty = apiConfigProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            subOrderRepository = subOrderRepository,
            operationHistoryRepository = operationHistoryRepository,
            paymentRepository = paymentRepository,
            getActionTypeDao = getActionTypeDao,
            util = util,
            getPaymentStatusDao = getPaymentStatusDao,
            getPaymentDao = getPaymentDao
        )
}