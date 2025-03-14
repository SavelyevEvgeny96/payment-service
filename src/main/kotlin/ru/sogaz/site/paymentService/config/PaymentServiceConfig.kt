package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.repository.*
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl

@Configuration
open class PaymentServiceConfig {
    @Bean
    open fun paymentService(
        configDataRepository: ConfigDataRepository,
        orderRepository: OrderRepository,
        subOrderRepository: SubOrderRepository,
        actionTypeRepository: ActionTypeRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        configDataDao: ConfigDataDao,
        paymentStatusRepository: PaymentStatusRepository,
        paymentRepository: PaymentRepository,
        paymentTypeRepository: PaymentTypeRepository
    ): PaymentService =
        PaymentServiceImpl(
            orderRepository = orderRepository,
            subOrderRepository = subOrderRepository,
            configDataRepository = configDataRepository,
            actionTypeRepository = actionTypeRepository,
            operationHistoryRepository = operationHistoryRepository,
            configDataDao = configDataDao,
            paymentStatusRepository = paymentStatusRepository,
            paymentRepository = paymentRepository,
            paymentTypeRepository = paymentTypeRepository
        )
}
