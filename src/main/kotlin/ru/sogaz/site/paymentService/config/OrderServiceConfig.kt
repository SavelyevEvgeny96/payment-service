package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl

@Configuration
open class OrderServiceConfig {
    @Bean
    open fun orderService(
        apiConfigProperty: ApiConfigProperties,
        clientSystemRepository: ClientSystemRepository,
        orderRepository: OrderRepository,
        orderStatusRepository: OrderStatusRepository,
        subOrderRepository: SubOrderRepository,
        getClientSystemDao: GetClientSystemDao,
        bankDao: BankDao,
        generatorService: GeneratorService,
        orderStatusDao: OrderStatusDao,
        orderDao: OrderDao,
    ): OrderService =
        OrderServiceImpl(
            apiConfigProperty = apiConfigProperty,
            orderRepository = orderRepository,
            subOrderRepository = subOrderRepository,
            bankDao = bankDao,
            getClientSystemDao = getClientSystemDao,
            generatorService = generatorService,
            orderStatusDao = orderStatusDao,
            orderDao = orderDao,
        )
}
