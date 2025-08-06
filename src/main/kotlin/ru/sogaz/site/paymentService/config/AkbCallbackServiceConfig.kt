package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.AkbCallbackService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.impl.AkbCallbackServiceImpl

@Configuration
class AkbCallbackServiceConfig {
    @Bean
    fun akbCallbackService(
        paymentRepository: PaymentRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentStatusService: PaymentStatusCheckerService,
        paymentStatusRepository: PaymentStatusRepository,
        actionTypeRepository: ActionTypeRepository,
        clientSystemRepository: ClientSystemRepository,
        getPaymentDao: GetPaymentDao,
        orderDao: OrderDao,
    ): AkbCallbackService =
        AkbCallbackServiceImpl(
            paymentRepository,
            paymentStatusRepository,
            actionTypeRepository,
            clientSystemRepository,
            operationHistoryRepository,
            paymentStatusService,
            getPaymentDao,
            orderDao,
        )
}
