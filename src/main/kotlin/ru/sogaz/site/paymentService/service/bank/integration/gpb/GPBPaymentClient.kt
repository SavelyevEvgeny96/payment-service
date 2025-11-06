package ru.sogaz.site.paymentService.service.bank.integration.gpb

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.clients.gpb.GpbCardPaymentClient
import ru.sogaz.site.paymentService.clients.gpb.GpbSbpPaymentClient
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBStatusSBPRequest
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.loggerFor

@Component
class GPBPaymentClient(
    private val gpbSbpPaymentClient: GpbSbpPaymentClient,
    private val gpbCardPaymentClient: GpbCardPaymentClient,
) {
    private val logger = loggerFor(javaClass)

    private fun getTokenForCardPay(portalId: String): Result<GazpromTokenResponse> = runCatching { gpbCardPaymentClient.getToken(portalId) }

    private fun registerCardPayment(
        portalId: String,
        request: GPBPaymentRequest,
    ): Result<GazpromCardPaymentResponse> = runCatching { gpbCardPaymentClient.startPayment(portalId, request.token, request) }

//    private fun registerSbpPayment(request: GPBSBPPaymentRequest): Result<GazpromSBPPaymentResponse> =
//        runCatching { gpbSbpPaymentClient.startPayment(request) }

    private fun getStatusCardPayment(
        portalId: String,
        paymentBankId: String,
    ): Result<GpbCardPaymentStatusResponse> = runCatching { gpbCardPaymentClient.getPaymentStatus(portalId, paymentBankId) }

    private fun getStatusSbpPayment(paymentBankId: String): Result<GpbSbpPaymentStatusResponse> =
        paymentBankId
            .run(::GPBStatusSBPRequest)
            .runCatching { gpbSbpPaymentClient.getPaymentStatus(this) }

//
//    private inline fun <reified T> postForObject(
//        url: String,
//        request: Any? = null,
//    ): T = postForObjectWithLogging(url, request)
//
//    private inline fun <reified T> postForObjectWithLogging(
//        url: String,
//        request: Any? = null,
//    ): T {
//        logger.info("Prepare for POST GPB request: $url with body: $request")
//        return withMeasureTime { runCatching { restTemplate.postForObject<T>(url, request) } }
//            .also { logger.info("Response from GPB: $it") }
//            .getOrThrow()
//    }
//
//    private inline fun <reified T> withMeasureTime(block: () -> T): T {
//        val (value: T, timeTaken) = measureTimedValue(block)
//        logger.info("${timeTaken.inWholeSeconds} whole seconds taken for GPB request")
//        return value
//    }
}
