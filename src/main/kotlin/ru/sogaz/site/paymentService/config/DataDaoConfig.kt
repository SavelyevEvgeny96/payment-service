package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.GetOrderStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.impl.BankDaoImpl
import ru.sogaz.site.paymentService.dao.impl.CallbackPaymentDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetActionTypeDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetClientSystemDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetOrderStatusDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetPaymentStatusDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetPaymentTypeDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetSubOrderDaoImpl
import ru.sogaz.site.paymentService.dao.impl.OrderDaoImpl
import ru.sogaz.site.paymentService.dao.impl.PaymentDaoImpl
import ru.sogaz.site.paymentService.dao.impl.PaymentOperationHistoryDaoImpl
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository

@Configuration
class DataDaoConfig {
    @Bean
    fun daoGetOrderConfig(
        orderRepository: OrderRepository,
        orderStatusRepository: OrderStatusRepository,
    ): OrderDao = OrderDaoImpl(orderRepository = orderRepository, orderStatusRepository = orderStatusRepository)

    @Bean
    fun daoGetSubOrderConfig(subOrderRepository: SubOrderRepository): GetSubOrderDao =
        GetSubOrderDaoImpl(subOrderRepository = subOrderRepository)

    @Bean
    fun daoGetPaymentTypeConfig(paymentTypeRepository: PaymentTypeRepository): GetPaymentTypeDao =
        GetPaymentTypeDaoImpl(paymentTypeRepository = paymentTypeRepository)

    @Bean
    fun daoGetPaymentStatusDaoConfig(paymentStatusRepository: PaymentStatusRepository): GetPaymentStatusDao =
        GetPaymentStatusDaoImpl(paymentStatusRepository = paymentStatusRepository)

    @Bean
    fun daoActionTypeConfig(actionTypeRepository: ActionTypeRepository): GetActionTypeDao =
        GetActionTypeDaoImpl(actionTypeRepository = actionTypeRepository)

    @Bean
    fun daoGetBankConfig(
        bankRepository: BankRepository,
        configDataRepository: ConfigDataRepository,
    ): BankDao =
        BankDaoImpl(
            bankRepository = bankRepository,
            configDataRepository = configDataRepository,
        )

    @Bean
    fun daoGetPaymentConfig(paymentRepository: PaymentRepository): PaymentDao = PaymentDaoImpl(paymentRepository = paymentRepository)

    @Bean
    fun callbackPaymentDaoConfig(callbackPaymentRepository: CallbackPaymentRepository): CallbackPaymentDao =
        CallbackPaymentDaoImpl(callbackPaymentRepository = callbackPaymentRepository)

    @Bean
    fun paymentOperationHistoryDao(paymentOperationHistoryRepository: PaymentOperationHistoryRepository): PaymentOperationHistoryDao =
        PaymentOperationHistoryDaoImpl(paymentOperationHistoryRepository = paymentOperationHistoryRepository)

    @Bean
    fun getOrderStatusDao(orderStatusRepository: OrderStatusRepository): GetOrderStatusDao = GetOrderStatusDaoImpl(orderStatusRepository)

    @Bean
    fun daoGetClientSystemConfig(clientSystemRepository: ClientSystemRepository): GetClientSystemDao =
        GetClientSystemDaoImpl(clientSystemRepository = clientSystemRepository)
}
