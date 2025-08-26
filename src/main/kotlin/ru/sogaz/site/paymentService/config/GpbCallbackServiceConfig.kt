package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ClientSystemRepository
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.SignatureVerifier
import ru.sogaz.site.paymentService.service.impl.GpbCallbackServiceImpl
import ru.sogaz.site.paymentService.service.impl.ReceiptServiceImpl.Companion.RECEIPT_GENERATED_ACTION

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
        getPaymentStatusDao: GetPaymentStatusDao,
        getOrderStatusDao: OrderStatusDao,
        apiConfigProperties: ApiConfigProperties,
    ): GpbCallbackService {
        val callbackActions =
            actionTypeRepository.findByActionName(RECEIPT_GENERATED_ACTION)

        val payClientSystems =
            clientSystemRepository.findByExternalSystemCode("PAY")

        return GpbCallbackServiceImpl(
            paymentDao = paymentDao,
            orderDao = orderDao,
            paymentOperationHistoryDao = paymentOperationHistoryDao,
            signatureVerifier = signatureVerifier,
            getPaymentStatusDao = getPaymentStatusDao,
            getOrderStatusDao = getOrderStatusDao,
            callbackAction = callbackActions,
            payClientSystem = payClientSystems,
            apiConfigProperties = apiConfigProperties,
        )
    }
}
