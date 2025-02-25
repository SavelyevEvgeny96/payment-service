package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl

@Configuration
open class PaymentServiceConfig {
    @Bean
    open fun paymentService(
        configDataRepository: ConfigDataRepository,
        apiConfig: ApiConfig,
        bankRepository: BankRepository,
        clientSystemRepository: ClientSystemRepository,
        orderRepository: OrderRepository,
        orderStatusRepository: OrderStatusRepository,
        subOrderRepository: SubOrderRepository,
    ): PaymentService =
        PaymentServiceImpl(
            apiConfig = apiConfig,
            bankRepository = bankRepository,
            clientSystemRepository = clientSystemRepository,
            orderRepository = orderRepository,
            orderStatusRepository = orderStatusRepository,
            subOrderRepository = subOrderRepository,
            configDataRepository = configDataRepository
        )
}
