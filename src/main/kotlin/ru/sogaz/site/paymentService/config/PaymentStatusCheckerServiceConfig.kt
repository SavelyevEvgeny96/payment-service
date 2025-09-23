package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.HistoryService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.payment.PaymentStatusCheckerServiceImpl

@Configuration
class PaymentStatusCheckerServiceConfig {
    @Bean
    open fun paymentStatusCheckerService(
        apiConfigProperty: ApiConfigProperties,
        restTemplate: WebConfigRestTemplate,
        receiptService: ReceiptService,
        rabbitTemplate: RabbitTemplate,
        objectMapper: ObjectMapper,
        rabbitProperties: RabbitProperties,
        subOrderDao: SubOrderDao,
        paymentDao: PaymentDao,
        operationHistoryDao: PaymentOperationHistoryDao,
        orderDao: OrderDao,
        historyService: HistoryService,
    ): PaymentStatusCheckerService =
        PaymentStatusCheckerServiceImpl(
            apiConfigProperty = apiConfigProperty,
            rabbitTemplate = rabbitTemplate,
            receiptService = receiptService,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            rabbit = rabbitProperties,
            subOrderDao = subOrderDao,
            paymentDao = paymentDao,
            operationHistoryDao = operationHistoryDao,
            orderDao = orderDao,
            historyService = historyService,
        )
}
