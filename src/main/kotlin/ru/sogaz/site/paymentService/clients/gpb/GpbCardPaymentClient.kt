package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GPBRefundResponseDto
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.dto.response.bank.SessionIdDtoResponse

@FeignClient(
    name = "gpb-card-payment-client",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardPaymentClient {
    @PostMapping(value = ["/{portalId}/token"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getToken(
        @PathVariable portalId: String,
    ): GazpromTokenResponse

    @PostMapping(value = ["/{portalId}/session/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getSessionId(
        @PathVariable portalId: String,
    ): SessionIdDtoResponse

    @PostMapping(
        value = ["/{portalId}/merchant/history/trx/{paymentBankId}/refund"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun startRefund(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String,
        @RequestHeader("X-IV-Authorization")
        sessionHeader: String,
        @RequestParam("amount") amount: Long,
        @RequestParam("currency") currency: String,
        @RequestParam("comment") comment: String,
    ): GPBRefundResponseDto

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GPBPaymentRequest,
    ): GazpromCardPaymentResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startPaymentRecurrent(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GPBPaymentRequest,
    ): RegisterCardResponseDto

    @GetMapping(value = ["/{portalId}/payment/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String,
    ): GpbCardPaymentStatusResponse
}
