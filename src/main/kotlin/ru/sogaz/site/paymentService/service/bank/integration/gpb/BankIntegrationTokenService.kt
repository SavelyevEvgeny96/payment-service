package ru.sogaz.site.paymentService.service.bank.integration.gpb

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.exceptions.BankIntegrationException
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.TokenService

@Service
class BankIntegrationTokenService(
    private val gpbCardPaymentClient: GpbCardPaymentClient,
    private val apiConfigProperties: ApiConfigProperties,
    private val paymentDao: PaymentDao,
) : TokenService {
    private val logger = loggerFor(javaClass)
    override fun exchangeForToken(depersonalization: Boolean): String? {
        return try {
            val portalId = takePortalId(depersonalization)
            gpbCardPaymentClient.getToken(portalId).token
        } catch (ex: Exception) {
            logger.error(ActionType.GET_ACCESS_TOKEN_ERROR.value + ex.message)
            null
        }
    }
    override fun saveToken(payment: Payment): String =
        (exchangeForToken(payment.depersonalization) ?: "").also { token ->
            payment.paymentBankId = token
            paymentDao.save(payment)
        }

    override fun takePortalId(depersonalization: Boolean): String =
        apiConfigProperties.mainPortalId
            .butIf(depersonalization) { apiConfigProperties.depersonalizedPortalId }

    override fun takeMerchantId(depersonalization: Boolean): String =
        apiConfigProperties.mainMerchantId
            .butIf(depersonalization) { apiConfigProperties.depersonalizedMerchantId }
}
