package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl

@Configuration
open class OrderServiceConfig {
    @Bean
    open fun orderService(
        configDataDao: ConfigDataDao,
        apiConfigProperty: ApiConfigProperty,
        clientSystemRepository: ClientSystemRepository,
        orderRepository: OrderRepository,
        orderStatusRepository: OrderStatusRepository,
        subOrderRepository: SubOrderRepository,
    ): OrderService =
        OrderServiceImpl(
            apiConfigProperty = apiConfigProperty,
            clientSystemRepository = clientSystemRepository,
            orderRepository = orderRepository,
            orderStatusRepository = orderStatusRepository,
            subOrderRepository = subOrderRepository,
            configDataDao = configDataDao,
        )
}
