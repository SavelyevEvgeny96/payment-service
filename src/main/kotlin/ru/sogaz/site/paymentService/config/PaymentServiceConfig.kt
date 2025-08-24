package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ActionTypeDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
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
        orderRepository: OrderRepository,
        subOrderRepository: SubOrderRepository,
        actionTypeRepository: ActionTypeRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentStatusRepository: PaymentStatusRepository,
        paymentRepository: PaymentRepository,
        paymentTypeRepository: PaymentTypeRepository,
        orderDao: OrderDao,
        subOrderDao: SubOrderDao,
        paymentStatusCheckerService: PaymentStatusCheckerService,
        paymentTypeDao: PaymentTypeDao,
        paymentStatusDao: PaymentStatusDao,
        actionTypeDao: ActionTypeDao,
        gazpromService: GazpromService,
        bankDao: BankDao,
        configDataDao: ConfigDataDao,
        akbBankIntegrationService: AkbBankIntegrationService
    ): PaymentService =
        PaymentServiceImpl(
            orderRepository = orderRepository,
            operationHistoryRepository = operationHistoryRepository,
            paymentRepository = paymentRepository,
            orderDao = orderDao,
            subOrderDao = subOrderDao,
            paymentTypeDao = paymentTypeDao,
            paymentStatusDao = paymentStatusDao,
            actionTypeDao = actionTypeDao,
            gazpromService = gazpromService,
            bankDao = bankDao,
            paymentStatusCheckerService = paymentStatusCheckerService,
            configDataDao = configDataDao,
            akbBankIntegrationService = akbBankIntegrationService
        )
}
