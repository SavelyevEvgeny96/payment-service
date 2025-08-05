package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import ru.sogaz.site.paymentService.util.Util

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
        getSubOrderDao: GetSubOrderDao,
        paymentStatusCheckerService: PaymentStatusCheckerService,
        getPaymentTypeDao: GetPaymentTypeDao,
        getPaymentStatusDao: GetPaymentStatusDao,
        getActionTypeDao: GetActionTypeDao,
        gazpromService: GazpromService,
        bankDao: BankDao,
    ): PaymentService =
        PaymentServiceImpl(
            orderRepository = orderRepository,
            operationHistoryRepository = operationHistoryRepository,
            paymentRepository = paymentRepository,
            orderDao = orderDao,
            getSubOrderDao = getSubOrderDao,
            getPaymentTypeDao = getPaymentTypeDao,
            getPaymentStatusDao = getPaymentStatusDao,
            getActionTypeDao = getActionTypeDao,
            gazpromService = gazpromService,
            bankDao = bankDao,
            paymentStatusCheckerService = paymentStatusCheckerService,
        )

    @Bean
    fun utilConfig() = Util(configDataRepository = configDataRepository)
}
