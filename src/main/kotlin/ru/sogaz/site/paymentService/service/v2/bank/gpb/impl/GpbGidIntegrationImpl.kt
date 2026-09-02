package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import feign.FeignException
import org.springframework.stereotype.Service
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.clients.gpb.GpbGidClient
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request.GpbGidMapper
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbExtResultCode
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid.GpbGidStatusRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.ClientCardDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidStatusResponse
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.request.pay.GidPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties
import ru.sogaz.site.paymentService.properties.gpb.GpbGidStatusRetryProperties
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbGidPayIntegration
import java.time.Instant
import java.util.UUID

/**
 * Сервис интеграции с Газпромбанком для первого шага оплаты банковской картой в ГИД.
 */
@Service
class GpbGidIntegrationImpl(
    private val gpbGidClient: GpbGidClient,
    private val gpbGidProperties: GpbGidProperties,
    private val gpbGidMapper: GpbGidMapper,
    private val retryProperties: GpbGidStatusRetryProperties,
) : GpbGidPayIntegration {
    companion object {
        private val RETRYABLE_STATUS_CODES = setOf(400, 500, 504)
        private const val RESULT_STATE = "RESULT"
    }

    private val log = loggerFor(javaClass)

    override fun gidPay(gidPayOperationRequest: GidPayOperationRequest): BankPaymentPageData =
        gpbGidMapper
            .toGpbGidPayRequest(gidPayOperationRequest, gpbGidProperties)
            .let { gpbGidClient.pay(getTraceId(), it) }
            .run(gpbGidMapper::toBankPaymentPageData)

    override fun payStatus(operation: IdempotentOrderOperation): BankOperationDetails {
        val paymentBankId = operation.paymentBankId
            ?: return wait(operation, "GPB GID status request skipped: paymentBankId is empty")
        val portalId =
            if (operation.depersonalization) {
                gpbGidProperties.depersonalizedPortalId
            } else {
                gpbGidProperties.portalId
            }
        val request = GpbGidStatusRequest(paymentBankId, portalId)

        return try {
            requestStatusWithRetry(request).toOperationDetails(paymentBankId)
        } catch (ex: Exception) {
            if (ex is InterruptedException) Thread.currentThread().interrupt()
            log.error("GPB GID status request failed: operationId={}, paymentBankId={}", operation.id, paymentBankId, ex)
            BankOperationDetails(paymentBankId, OperationState.WAIT)
        }
    }

    private fun requestStatusWithRetry(request: GpbGidStatusRequest): GpbGidStatusResponse {
        var retries = 0
        while (true) {
            try {
                return gpbGidClient.getPaymentStatus(statusHeaders(), request)
            } catch (ex: FeignException) {
                if (ex.status() !in RETRYABLE_STATUS_CODES || retries >= retryProperties.maxRetries) throw ex
                retries++
                log.warn(
                    "Retrying GPB GID status request after HTTP {}: pgaTrxId={}, retry={}/{}",
                    ex.status(),
                    request.pgaTrxId,
                    retries,
                    retryProperties.maxRetries,
                )
                Thread.sleep(retryProperties.timeoutMs)
            }
        }
    }

    private fun statusHeaders(): Map<String, String> =
        buildMap {
            put("Accept-Language", "ru")
            put("X-Correlation-Id", UUID.randomUUID().toString())
            put("Content-Type", "application/json")
            gpbGidProperties.cookie.takeIf(String::isNotBlank)?.let { put("Cookie", it) }
        }

    private fun GpbGidStatusResponse.toOperationDetails(expectedPaymentBankId: String): BankOperationDetails {
        if (pgaTrxId != expectedPaymentBankId) {
            log.error(
                "GPB GID status response transaction mismatch: expected={}, actual={}",
                expectedPaymentBankId,
                pgaTrxId,
            )
            return BankOperationDetails(expectedPaymentBankId, OperationState.WAIT)
        }
        if (!state.equals(RESULT_STATE, ignoreCase = true) || result == null) {
            return BankOperationDetails(expectedPaymentBankId, OperationState.WAIT)
        }

        val operationState =
            when (result.status.trim().uppercase()) {
                "SUCCESS" -> OperationState.SUCCESS
                "FAILED", "REFUND", "DECLINED" -> OperationState.FAIL
                else -> OperationState.WAIT
            }
        if (operationState == OperationState.WAIT) {
            return BankOperationDetails(expectedPaymentBankId, operationState)
        }

        val extendedCode = result.extendedCode
        val knownExtendedCode = GpbExtResultCode.from(extendedCode)
        return BankOperationDetails(
            bankId = expectedPaymentBankId,
            state = operationState,
            operationFinished = Instant.ofEpochMilli(actualTimestamp),
            extendedCode = if (operationState == OperationState.FAIL) extendedCode else null,
            cardDetails = card?.let {
                ClientCardDetails(
                    maskedPan = it.maskedPan,
                    paymentSystem = it.paymentSystem,
                    issuerName = it.issuerName,
                    paymentType = result.srcType,
                    cardId = null,
                    title = null,
                )
            },
            errorText = if (operationState == OperationState.FAIL) knownExtendedCode?.message ?: extendedCode else null,
        )
    }

    private fun wait(operation: IdempotentOrderOperation, message: String): BankOperationDetails {
        log.error("{}: operationId={}", message, operation.id)
        return BankOperationDetails(operation.paymentBankId, OperationState.WAIT)
    }
}
