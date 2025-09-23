package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.payment.GeneratorServiceImpl
import ru.sogaz.site.paymentService.service.payment.QRCodeServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi

@Configuration
class GeneratorConfig {
    @Bean
    fun generatorServiceConfig(): GeneratorService = GeneratorServiceImpl()

    @Bean
    fun qrGeneratorServiceConfig(bankIntegrationFactoryService: BankIntegrationFactoryService): QRCodeService =
        QRCodeServiceImpl(
            qrCodeControllerApi = QrCodeControllerApi(),
            bankIntegrationFactoryService = bankIntegrationFactoryService,
        )
}
