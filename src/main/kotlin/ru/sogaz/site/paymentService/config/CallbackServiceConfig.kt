package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.CallbackService
import ru.sogaz.site.paymentService.service.impl.CallbackServiceImpl

@Configuration
class CallbackServiceConfig(
    private val paymentStatusRepository: PaymentStatusRepository,
    private val clientSystemRepository: ClientSystemRepository,
) {
    @Bean
    fun akbCallbackService(
        paymentDao: PaymentDao,
        orderDao: OrderDao,
        callbackPaymentRepository: CallbackPaymentRepository,
        callbackPaymentDao: CallbackPaymentDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
    ): CallbackService {
        val callbackPaymentsStatus =
            paymentStatusRepository.findByStateId("CALLBACK")
                ?: throw IllegalStateException("Cтатус платежа CALLBACK -  не найден")

        val payClientSystems =
            clientSystemRepository.findByExternalSystemCode("PAY")
                ?: throw IllegalStateException("Автор для PAY не найден")

        return CallbackServiceImpl(
            paymentDao = paymentDao,
            orderDao = orderDao,
            callbackPaymentStatus = callbackPaymentsStatus,
            payClientSystem = payClientSystems,
            callbackPaymentDao = callbackPaymentDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
        )
    }
}
