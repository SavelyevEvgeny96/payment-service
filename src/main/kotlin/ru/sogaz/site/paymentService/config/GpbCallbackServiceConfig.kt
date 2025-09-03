package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.CallbackPaymentDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.GpbCallbackServiceImpl

@Configuration
class GpbCallbackServiceConfig(
    private val actionTypeRepository: ActionTypeRepository,
    private val clientSystemRepository: ClientSystemRepository,
) {
    @Bean
    fun gpbCallbackService(
        paymentDao: PaymentDao,
        orderDao: OrderDao,
        paymentOperationHistoryDao: PaymentOperationHistoryDao,
        signatureVerifier: SignatureVerifier,
        paymentStatusDao: PaymentStatusDao,
        getOrderStatusDao: OrderStatusDao,
        apiConfigProperties: ApiConfigProperties,
        callbackPaymentDao: CallbackPaymentDao,
    ): GpbCallbackService {
        val payClientSystems =
            clientSystemRepository.findByExternalSystemCode("PAY")

        return GpbCallbackServiceImpl(
            paymentDao = paymentDao,
            orderDao = orderDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
            signatureVerifier = signatureVerifier,
            paymentStatusDao = paymentStatusDao,
            getOrderStatusDao = getOrderStatusDao,
            payClientSystem = payClientSystems,
            apiConfigProperties = apiConfigProperties,
            callbackPaymentDao = callbackPaymentDao,
        )
    }
}
