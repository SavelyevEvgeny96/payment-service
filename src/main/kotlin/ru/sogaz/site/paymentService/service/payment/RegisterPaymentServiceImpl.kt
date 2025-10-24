package ru.sogaz.site.paymentService.service.payment

import java.time.LocalDateTime
import org.springframework.web.client.RestClientException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService

class RegisterPaymentServiceImpl(
    private val paymentDao: PaymentDao,
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao,
    private val bankIntegrationFactoryService: BankIntegrationFactoryService,
) : RegisterPaymentService {
    companion object {
        const val ERROR_PAYMENT_PROCESSING = "Payment processing error"
    }

    private val logger = loggerFor(javaClass)

    override fun register(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
    ): Payment =
        try {
            formPayment(order, paymentTypeEnum, urlToReturn)
                .run { paymentDao.save(this) }
                .also { saveHistory(order, ActionType.SEND_PAYMENT_START_REQUEST) }
                .run { registerInBank(this) }
                .apply { paymentStarted = LocalDateTime.now() }
                .run { paymentDao.save(this) }
                .also { saveHistory(order, ActionType.GET_PAYMENT_LINK) }
        } catch (ex: Exception) {
            logger.error("ERROR: " + ex.message)
            saveHistory(order, ActionType.PAYMENT_START_REQUEST_ERROR)
            when (ex) {
                is RestClientException -> throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
                is BankIntegrationException -> {
                    saveHistory(order, ex.actionType)
                    throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, getTraceId())
                }
                else -> throw InnerException(getTraceId(), ERROR_PAYMENT_PROCESSING)
            }
        }

    private fun formPayment(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
    ): Payment =
        Payment(
            bank = order.bank,
            order = order,
            type = paymentTypeEnum,
            urlToReturn = urlToReturn,
        )

    private fun saveHistory(
        order: Order,
        actionType: ActionType,
    ) = paymentOperationHistoryDao.saveForOrder(order, actionType.value)

    private fun registerInBank(payment: Payment): Payment =
        bankIntegrationFactoryService
            .getInstanceByBank(payment.bank)
            .run { this.registerPayment(payment) }
}
