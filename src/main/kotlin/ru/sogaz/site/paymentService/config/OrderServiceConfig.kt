package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetBankDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.GetOrderStatusDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl
import ru.sogaz.site.paymentService.util.Util

@Configuration
open class OrderServiceConfig {
    @Bean
    open fun orderService(
        apiConfigProperty: ApiConfigProperty,
        clientSystemRepository: ClientSystemRepository,
        orderRepository: OrderRepository,
        orderStatusRepository: OrderStatusRepository,
        subOrderRepository: SubOrderRepository,
        util: Util,
        getClientSystemDao: GetClientSystemDao,
        getOrderStatusDao: GetOrderStatusDao,
        getBankDao: GetBankDao,
    ): OrderService =
        OrderServiceImpl(
            apiConfigProperty = apiConfigProperty,
            orderRepository = orderRepository,
            subOrderRepository = subOrderRepository,
            util = util,
            getBankDao = getBankDao,
            getClientSystemDao = getClientSystemDao,
            getOrderStatusDao = getOrderStatusDao,
        )
}
