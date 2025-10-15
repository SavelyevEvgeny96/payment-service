package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.mapper.SubOrderMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.SubOrderService
import ru.sogaz.site.paymentService.service.order.OrderServiceImpl
import ru.sogaz.site.paymentService.service.order.SubOrderServiceImpl

@Configuration
open class OrderServiceConfig {
    @Bean
    open fun orderService(
        apiConfigProperty: ApiConfigProperties,
        orderDao: OrderDao,
    ): OrderService =
        OrderServiceImpl(
            apiConfigProperty = apiConfigProperty,
            orderDao = orderDao,
        )

    @Bean
    fun subOrderService(
        subOrderDao: SubOrderDao,
        subOrderMapper: SubOrderMapper,
    ): SubOrderService = SubOrderServiceImpl(subOrderDao, subOrderMapper)
}
