package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.service.AkbBankIntegrationService
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl

@Configuration
open class PaymentServiceConfig(
    private val configDataRepository: ConfigDataRepository,
) {
    @Bean
    open fun paymentService(
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
        paymentStatusCheckerService: PaymentStatusCheckerService,
        paymentTypeDao: PaymentTypeDao,
        paymentStatusDao: PaymentStatusDao,
        gazpromService: GazpromService,
        bankDao: BankDao,
        configDataDao: ConfigDataDao,
        akbBankIntegrationService: AkbBankIntegrationService,
        paymentDao: PaymentDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
    ): PaymentService =
        PaymentServiceImpl(
            orderDao = orderDao,
            subOrderDao = subOrderDao,
            paymentTypeDao = paymentTypeDao,
            paymentStatusDao = paymentStatusDao,
            gazpromService = gazpromService,
            bankDao = bankDao,
            paymentStatusCheckerService = paymentStatusCheckerService,
            configDataDao = configDataDao,
            akbBankIntegrationService = akbBankIntegrationService,
            paymentDao = paymentDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
        )
}
