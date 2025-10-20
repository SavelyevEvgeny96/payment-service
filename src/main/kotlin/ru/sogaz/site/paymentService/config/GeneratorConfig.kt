package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.properties.QrConfigProperties
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.payment.GeneratorServiceImpl
import ru.sogaz.site.paymentService.service.payment.QRCodeServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi
import ru.sogaz.site.qr.generator.client.invoker.ApiClient

@Configuration
class GeneratorConfig(
    val qrConfigProperties: QrConfigProperties,
) {
    @Bean
    fun generatorServiceConfig(): GeneratorService = GeneratorServiceImpl()

    @Bean
    fun qrGeneratorServiceConfig(bankIntegrationFactoryService: BankIntegrationFactoryService): QRCodeService =
        QRCodeServiceImpl(
            qrCodeControllerApi = qrCodeControllerApi(),
            bankIntegrationFactoryService = bankIntegrationFactoryService,
        )

    private fun qrCodeControllerApi() =
        ApiClient()
            .apply { basePath = qrConfigProperties.baseUrl }
            .run(::QrCodeControllerApi)
}
