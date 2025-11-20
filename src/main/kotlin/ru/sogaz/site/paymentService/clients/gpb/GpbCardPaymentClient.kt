package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.dto.request.GPBPaymentRequest
import ru.sogaz.site.paymentService.dto.response.GazpromCardPaymentResponse
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto

@FeignClient(
    name = "gpb-card-payment-client",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardPaymentClient {
    @PostMapping(value = ["/{portalId}/token"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getToken(
        @PathVariable portalId: String,
    ): GazpromTokenResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startPayment(
        @PathVariable portalId: String,
        @PathVariable token: String?,
        @RequestBody request: GPBPaymentRequest,
    ): GazpromCardPaymentResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startPaymentRecurrent(
        @PathVariable portalId: String,
        @PathVariable token: String?,
        @RequestBody request: GPBPaymentRequest,
    ): RegisterCardResponseDto

    @GetMapping(value = ["/{portalId}/payment/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String,
    ): GpbCardPaymentStatusResponse
}
