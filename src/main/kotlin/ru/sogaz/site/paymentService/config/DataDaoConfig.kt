package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.ClientSystemDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dao.impl.BankDaoImpl
import ru.sogaz.site.paymentService.dao.impl.CallbackPaymentDaoImpl
import ru.sogaz.site.paymentService.dao.impl.ClientSystemDaoImpl
import ru.sogaz.site.paymentService.dao.impl.ConfigDataDaoImpl
import ru.sogaz.site.paymentService.dao.impl.OrderDaoImpl
import ru.sogaz.site.paymentService.dao.impl.PaymentDaoImpl
import ru.sogaz.site.paymentService.dao.impl.PaymentOperationHistoryDaoImpl
import ru.sogaz.site.paymentService.dao.impl.PaymentTypeDaoImpl
import ru.sogaz.site.paymentService.dao.impl.SubOrderDaoImpl
import ru.sogaz.site.paymentService.dao.impl.WaitingPaymentDaoImpl
import ru.sogaz.site.paymentService.repository.BankRepository
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentTypeRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.repository.WaitingPaymentRepository

@Configuration
class DataDaoConfig {
    @Bean
    fun daoGetOrderConfig(orderRepository: OrderRepository): OrderDao = OrderDaoImpl(orderRepository)

    @Bean
    fun daoGetSubOrderConfig(subOrderRepository: SubOrderRepository): SubOrderDao = SubOrderDaoImpl(subOrderRepository = subOrderRepository)

    @Bean
    fun daoGetPaymentTypeConfig(paymentTypeRepository: PaymentTypeRepository): PaymentTypeDao =
        PaymentTypeDaoImpl(paymentTypeRepository = paymentTypeRepository)

    @Bean
    fun daoGetBankConfig(
        bankRepository: BankRepository,
        configDataDao: ConfigDataDao,
    ): BankDao =
        BankDaoImpl(
            bankRepository = bankRepository,
            configDataDao = configDataDao,
        )

    @Bean
    fun daoConfigData(configDataRepository: ConfigDataRepository): ConfigDataDao =
        ConfigDataDaoImpl(configDataRepository = configDataRepository)

    @Bean
    fun daoGetPaymentConfig(
        paymentRepository: PaymentRepository,
        paymentTypeDao: PaymentTypeDao,
    ): PaymentDao =
        PaymentDaoImpl(
            paymentRepository = paymentRepository,
            paymentTypeDao = paymentTypeDao,
        )

    @Bean
    fun callbackPaymentDaoConfig(callbackPaymentRepository: CallbackPaymentRepository): CallbackPaymentDao =
        CallbackPaymentDaoImpl(callbackPaymentRepository = callbackPaymentRepository)

    @Bean
    fun waitingPaymentDaoConfig(waitingPaymentRepository: WaitingPaymentRepository): WaitingPaymentDao =
        WaitingPaymentDaoImpl(waitingPaymentRepository = waitingPaymentRepository)

    @Bean
    fun paymentOperationHistoryDao(paymentOperationHistoryRepository: PaymentOperationHistoryRepository): PaymentOperationHistoryDao =
        PaymentOperationHistoryDaoImpl(
            paymentOperationHistoryRepository = paymentOperationHistoryRepository,
        )

    @Bean
    fun daoGetClientSystemConfig(clientSystemRepository: ClientSystemRepository): ClientSystemDao =
        ClientSystemDaoImpl(clientSystemRepository = clientSystemRepository)
}
