package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ClientSystemDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl

@Configuration
open class OrderServiceConfig {
    @Bean
    open fun orderService(
        apiConfigProperty: ApiConfigProperties,
        clientSystemDao: ClientSystemDao,
        bankDao: BankDao,
        generatorService: GeneratorService,
        orderStatusDao: OrderStatusDao,
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
    ): OrderService =
        OrderServiceImpl(
            apiConfigProperty = apiConfigProperty,
            bankDao = bankDao,
            clientSystemDao = clientSystemDao,
            generatorService = generatorService,
            orderStatusDao = orderStatusDao,
            orderDao = orderDao,
            subOrderDao = subOrderDao,
        )
}
