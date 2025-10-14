package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.service.callback.CallbackServiceImpl

@Configuration
class CallbackServiceConfig(
    private val clientSystemRepository: ClientSystemRepository,
) {
    @Bean
    fun akbCallbackService(
        paymentDao: PaymentDao,
        orderDao: OrderDao,
        callbackPaymentRepository: CallbackPaymentRepository,
        callbackPaymentDao: CallbackPaymentDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
    ) = CallbackServiceImpl(
        paymentDao = paymentDao,
        orderDao = orderDao,
        callbackPaymentDao = callbackPaymentDao,
        paymentOperationHistoryDao = paymentOperationHistoryDao,
    )
}
