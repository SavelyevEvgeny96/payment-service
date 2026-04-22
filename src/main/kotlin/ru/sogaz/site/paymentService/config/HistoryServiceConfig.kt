package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.service.HistoryService
import ru.sogaz.site.paymentService.service.order.HistoryServiceImpl

@Configuration
open class HistoryServiceConfig {
    @Bean
    open fun historyService(
        subOrderDao: SubOrderDao,
        operationHistoryDao: PaymentOperationHistoryDao,
    ): HistoryService = HistoryServiceImpl(subOrderDao, operationHistoryDao)
}
