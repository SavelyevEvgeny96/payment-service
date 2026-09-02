package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.clients.gpb.GpbGidClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbGidMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbGidStatusRequestMapper
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response.GpbGidStatusResponseMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GidPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbGidPayIntegration
import java.util.UUID

/**
 * Сервис интеграции с Газпромбанком для первого шага оплаты банковской картой в ГИД.
 */
@Service
class GpbGidIntegrationImpl(
    private val gpbGidClient: GpbGidClient,
    private val gpbGidProperties: GpbGidProperties,
    private val gpbGidMapper: GpbGidMapper,
    private val statusRequestMapper: GpbGidStatusRequestMapper,
    private val statusResponseMapper: GpbGidStatusResponseMapper,
) : GpbGidPayIntegration {
    companion object {
        private const val OPERATION_DETAILS_ERROR =
            "Во время получения данных по операции оплаты картой через ГИД произошла ошибка: {}"
    }

    private val log = loggerFor(javaClass)

    override fun gidPay(gidPayOperationRequest: GidPayOperationRequest): BankPaymentPageData =
        gpbGidMapper
            .toGpbGidPayRequest(gidPayOperationRequest, gpbGidProperties)
            .let { gpbGidClient.pay(getTraceId(), it) }
            .run(gpbGidMapper::toBankPaymentPageData)

    override fun payStatus(operation: IdempotentOrderOperation): BankOperationDetails {
        val paymentBankId = operation.paymentBankId ?: return BankOperationDetails(null, OperationState.WAIT)

        return try {
            val request =
                statusRequestMapper.toGpbGidStatusRequest(
                    paymentBankId,
                    operation.depersonalization,
                    gpbGidProperties,
                )
            val response = gpbGidClient.getPaymentStatus(statusHeaders(), request)
            statusResponseMapper.toBankOperationDetails(response, paymentBankId)
        } catch (ex: Exception) {
            log.error(OPERATION_DETAILS_ERROR, ex.message, ex)
            BankOperationDetails(paymentBankId, OperationState.WAIT)
        }
    }

    private fun statusHeaders(): Map<String, String> =
        buildMap {
            put("Accept-Language", "ru")
            put("X-Correlation-Id", UUID.randomUUID().toString())
            put("Content-Type", "application/json")
            gpbGidProperties.cookie.takeIf(String::isNotBlank)?.let { put("Cookie", it) }
        }
}
