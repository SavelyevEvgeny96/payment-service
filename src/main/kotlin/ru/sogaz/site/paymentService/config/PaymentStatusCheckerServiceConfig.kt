package ru.sogaz.site.paymentService.config

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl

@Configuration
class PaymentStatusCheckerServiceConfig {
    @Bean
    open fun paymentStatusCheckerService(
        configDataRepository: ConfigDataRepository,
        orderRepository: OrderRepository,
        subOrderRepository: SubOrderRepository,
        actionTypeRepository: ActionTypeRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        configDataDao: ConfigDataDao,
        paymentStatusRepository: PaymentStatusRepository,
        paymentRepository: PaymentRepository,
        apiConfigProperty: ApiConfigProperty,
        restTemplate: RestTemplate,
        receiptService: ReceiptService,
        orderStatusRepository: OrderStatusRepository,
        rabbitTemplate: RabbitTemplate,
    ): PaymentStatusCheckerService =
        PaymentStatusCheckerServiceImpl(
            orderRepository = orderRepository,
            subOrderRepository = subOrderRepository,
            configDataRepository = configDataRepository,
            actionTypeRepository = actionTypeRepository,
            operationHistoryRepository = operationHistoryRepository,
            configDataDao = configDataDao,
            paymentStatusRepository = paymentStatusRepository,
            paymentRepository = paymentRepository,
            apiConfigProperty = apiConfigProperty,
            orderStatusRepository = orderStatusRepository,
            rabbitTemplate = rabbitTemplate,
            receiptService = receiptService,
            restTemplate = restTemplate,
        )
}
