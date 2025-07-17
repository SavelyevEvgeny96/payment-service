package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentService.properties.ReceiptProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ChequeSentRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.ReceiptServiceImpl

@Configuration
open class ReceiptServiceConfig {
    @Bean
    open fun receiptService(
        subOrderRepository: SubOrderRepository,
        actionTypeRepository: ActionTypeRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        receiptProperty: ReceiptProperty,
        restTemplate: RestTemplate,
        objectMapper: ObjectMapper,
        paymentRepository: PaymentRepository,
        chequeSentRepository: ChequeSentRepository,
    ): ReceiptService =
        ReceiptServiceImpl(
            subOrderRepository = subOrderRepository,
            actionTypeRepository = actionTypeRepository,
            operationHistoryRepository = operationHistoryRepository,
            receiptProperty = receiptProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            paymentRepository = paymentRepository,
            chequeSentRepository = chequeSentRepository,
        )
}
