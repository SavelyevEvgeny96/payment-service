package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl

@Configuration
class PaymentStatusCheckerServiceConfig {
    @Bean
    open fun paymentStatusCheckerService(
        orderRepository: OrderRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentStatusRepository: PaymentStatusRepository,
        paymentRepository: PaymentRepository,
        apiConfigProperty: ApiConfigProperties,
        restTemplate: WebConfigRestTemplate,
        receiptService: ReceiptService,
        orderStatusRepository: OrderStatusRepository,
        rabbitTemplate: RabbitTemplate,
        objectMapper: ObjectMapper,
        rabbitProperties: RabbitProperties,
        subOrderDao: SubOrderDao,
    ): PaymentStatusCheckerService =
        PaymentStatusCheckerServiceImpl(
            orderRepository = orderRepository,
            operationHistoryRepository = operationHistoryRepository,
            paymentStatusRepository = paymentStatusRepository,
            paymentRepository = paymentRepository,
            apiConfigProperty = apiConfigProperty,
            orderStatusRepository = orderStatusRepository,
            rabbitTemplate = rabbitTemplate,
            receiptService = receiptService,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            rabbit = rabbitProperties,
            subOrderDao = subOrderDao,
        )
}
