package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.GpbCallbackServiceImpl

@Configuration
class GpbCallbackServiceConfig {
    @Bean
    fun gpbCallbackService(
        paymentRepository: PaymentRepository,
        orderRepository: OrderRepository,
        operationHistoryRepository: PaymentOperationHistoryRepository,
        paymentStatusService: PaymentStatusCheckerService,
        signatureVerifier: SignatureVerifier,
        paymentStatusRepository: PaymentStatusRepository,
        actionTypeRepository: ActionTypeRepository,
        clientSystemRepository: ClientSystemRepository,
    ): GpbCallbackService =
        GpbCallbackServiceImpl(
            paymentRepository,
            orderRepository,
            operationHistoryRepository,
            paymentStatusService,
            signatureVerifier,
            paymentStatusRepository,
            actionTypeRepository,
            clientSystemRepository,
        )
}
