package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.payment.PaymentServiceImpl
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService

@Configuration
open class PaymentServiceConfig(
    private val configDataRepository: ConfigDataRepository,
) {
    @Bean
    fun registerPaymentService(
        paymentDao: PaymentDao,
        orderDao: OrderDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
        bankIntegrationFactoryService: BankIntegrationFactoryService,
    ): RegisterPaymentService =
        RegisterPaymentServiceImpl(
            paymentDao = paymentDao,
            orderDao = orderDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
            bankIntegrationFactoryService = bankIntegrationFactoryService,
        )

    @Bean
    open fun paymentService(
        orderDao: OrderDao,
        bankDao: BankDao,
        configDataDao: ConfigDataDao,
        registerPaymentService: RegisterPaymentService,
        qrCodeService: QRCodeService,
        apiConfigProperties: ApiConfigProperties,
    ): PaymentService =
        PaymentServiceImpl(
            orderDao = orderDao,
            bankDao = bankDao,
            configDataDao = configDataDao,
            registerPaymentService = registerPaymentService,
            qrCodeService = qrCodeService,
            apiConfigProperties = apiConfigProperties,
        )
}
