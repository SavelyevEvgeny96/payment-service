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
    fun paymentReceiptControllerApi(): PaymentReceiptControllerApi {
        val apiClient = ApiClient().setBasePath(receiptProperties.receiptUrl)
        return PaymentReceiptControllerApi(apiClient)
    }
}
