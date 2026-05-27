package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse

@FeignClient(
    name = "gpb-card-client",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardClient {
    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun cardPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GpbPayRequest,
    ): GpbPayCardResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun cardRecurrentPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GpbPayRequest,
    ): GpbCardPayDetailsResponse

    @GetMapping(value = ["/{portalId}/payment/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String?,
    ): GpbCardPayDetailsResponse
}
