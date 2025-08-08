package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.CallbackPaymentRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.AkbCallbackService
import ru.sogaz.site.paymentService.service.impl.AkbCallbackServiceImpl

@Configuration
class AkbCallbackServiceConfig(
    private val paymentStatusRepository: PaymentStatusRepository,
    private val actionTypeRepository: ActionTypeRepository,
    private val clientSystemRepository: ClientSystemRepository,
) {
    @Bean
    fun akbCallbackService(
        paymentDao: PaymentDao,
        orderDao: OrderDao,
        callbackPaymentRepository: CallbackPaymentRepository,
        callbackPaymentDao: CallbackPaymentDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
    ): AkbCallbackService {
        val callbackPaymentsStatus =
            paymentStatusRepository.findByStateId("CALLBACK_AKB")
                ?: throw IllegalStateException("Cтатус платежа CALLBACK_AKB не найден")

        val callbackActions =
            actionTypeRepository.findByActionName("Получение CALLBACK от АКБ Россия")
                ?: throw IllegalStateException("ActionType для CALLBACK_SUCCESS не найден")

        val payClientSystems =
            clientSystemRepository.findByExternalSystemCode("PAY")
                ?: throw IllegalStateException("Автор для PAY не найден")

        return AkbCallbackServiceImpl(
            paymentDao = paymentDao,
            orderDao = orderDao,
            callbackPaymentStatus = callbackPaymentsStatus,
            callbackAction = callbackActions,
            payClientSystem = payClientSystems,
            callbackPaymentDao = callbackPaymentDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
        )
    }
}
