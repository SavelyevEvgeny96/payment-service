package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetOrderDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import ru.sogaz.site.paymentService.util.Util

@Configuration
open class PaymentServiceConfig {
    @Bean
    open fun paymentService(
        configDataRepository: ConfigDataRepository,
        orderRepository: OrderRepository,
        subOrderRepository: SubOrderRepository,
        actionTypeRepository: ActionTypeRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        configDataDao: ConfigDataDao,
        paymentStatusRepository: PaymentStatusRepository,
        paymentRepository: PaymentRepository,
        paymentTypeRepository: PaymentTypeRepository,
        getOrderDao: GetOrderDao,
        getSubOrderDao: GetSubOrderDao,
        util: Util,
        getPaymentTypeDao: GetPaymentTypeDao,
        getPaymentStatusDao: GetPaymentStatusDao,
        getActionTypeDao: GetActionTypeDao
    ): PaymentService =
        PaymentServiceImpl(
            orderRepository = orderRepository,
            configDataRepository = configDataRepository,
            operationHistoryRepository = operationHistoryRepository,
            configDataDao = configDataDao,
            paymentRepository = paymentRepository,
            getOrderDao = getOrderDao,
            getSubOrderDao = getSubOrderDao,
            util = util,
            getPaymentTypeDao = getPaymentTypeDao,
            getPaymentStatusDao = getPaymentStatusDao,
            getActionTypeDao = getActionTypeDao
        )

    @Bean
    fun utilConfig() = Util()
}
