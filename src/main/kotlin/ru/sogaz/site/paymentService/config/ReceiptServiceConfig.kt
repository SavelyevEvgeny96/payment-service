package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.payment.receipt.client.api.PaymentReceiptControllerApi
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.payment.receipt.client.invoker.ApiClient
import ru.sogaz.site.paymentService.properties.ReceiptProperties
import ru.sogaz.site.paymentService.repository.ChequeSentRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl

@Configuration
open class ReceiptServiceConfig(
    private val receiptProperties: ReceiptProperties
) {
    @Bean
    fun paymentReceiptControllerApi(): PaymentReceiptControllerApi {
        val apiClient = ApiClient().setBasePath(receiptProperties.receiptUrl)
        return PaymentReceiptControllerApi(apiClient)
    }

    @Bean
    open fun receiptService(
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentReceiptControllerApi: PaymentReceiptControllerApi,
        paymentRepository: PaymentRepository,
        chequeSentRepository: ChequeSentRepository,
        subOrderDao: SubOrderDao,
    ): ReceiptService =
        ReceiptServiceImpl(
            operationHistoryRepository = operationHistoryRepository,
            paymentReceiptControllerApi = paymentReceiptControllerApi,
            paymentRepository = paymentRepository,
            chequeSentRepository = chequeSentRepository,
            subOrderDao = subOrderDao,
        )
}
