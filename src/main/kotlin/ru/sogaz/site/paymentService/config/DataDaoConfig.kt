package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetBankDao
import ru.sogaz.site.paymentService.dao.GetBankPriorityDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.GetOrderDao
import ru.sogaz.site.paymentService.dao.GetOrderStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.dao.impl.GetActionTypeDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetBankDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetBankPriorityDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetClientSystemDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetOrderDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetOrderStatusDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetPaymentDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetPaymentStatusDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetPaymentTypeDaoImpl
import ru.sogaz.site.paymentService.dao.impl.GetSubOrderDaoImpl
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.BankRepository
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
    fun daoGetOrderConfig(orderRepository: OrderRepository): GetOrderDao =
        GetOrderDaoImpl(orderRepository = orderRepository)

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
    fun daoGetBankConfig(bankRepository: BankRepository, getBankPriorityDao: GetBankPriorityDao): GetBankDao =
        GetBankDaoImpl(
            bankRepository = bankRepository,
            getBankPriorityDao = getBankPriorityDao
        )

    @Bean
    fun daoGetBankPriority(configDataRepository: ConfigDataRepository): GetBankPriorityDao = GetBankPriorityDaoImpl(
        configDataRepository = configDataRepository
    )

    @Bean
    fun daoGetPaymentConfig(paymentRepository: PaymentRepository): GetPaymentDao =
        GetPaymentDaoImpl(paymentRepository = paymentRepository)

    @Bean
    fun daoGetClientSystemConfig(clientSystemRepository: ClientSystemRepository): GetClientSystemDao =
        GetClientSystemDaoImpl(clientSystemRepository = clientSystemRepository)

    @Bean
    fun daoGetOrderStatusConfig(orderStatusRepository: OrderStatusRepository): GetOrderStatusDao =
        GetOrderStatusDaoImpl(orderStatusRepository = orderStatusRepository)
}
