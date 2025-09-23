package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.properties.ReceiptProperties
import ru.sogaz.site.paymentService.repository.ChequeSentRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl

@Configuration
open class ReceiptServiceConfig {
    @Bean
    open fun receiptService(
        operationHistoryRepository: PaymentOperationHistoryRepository,
        receiptProperty: ReceiptProperties,
        restTemplate: WebConfigRestTemplate,
        objectMapper: ObjectMapper,
        paymentRepository: PaymentRepository,
        chequeSentRepository: ChequeSentRepository,
        subOrderDao: SubOrderDao,
    ): ReceiptService =
        ReceiptServiceImpl(
            operationHistoryRepository = operationHistoryRepository,
            receiptProperty = receiptProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            paymentRepository = paymentRepository,
            chequeSentRepository = chequeSentRepository,
            subOrderDao = subOrderDao,
        )
}
