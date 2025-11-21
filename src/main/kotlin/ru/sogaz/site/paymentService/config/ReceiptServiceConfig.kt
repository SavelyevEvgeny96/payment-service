package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.payment.receipt.client.api.PaymentReceiptControllerApi
import ru.sogaz.site.payment.receipt.client.invoker.ApiClient
import ru.sogaz.site.paymentService.properties.ReceiptProperties

@Configuration
open class ReceiptServiceConfig(
    private val receiptProperties: ReceiptProperties,
) {
    @Bean
    fun receiptApiClient(): ApiClient =
        ApiClient().apply {
            basePath = receiptProperties.receiptUrl
        }

    @Bean
    fun paymentReceiptControllerApi(receiptApiClient: ApiClient): PaymentReceiptControllerApi =
        PaymentReceiptControllerApi(receiptApiClient)
}
