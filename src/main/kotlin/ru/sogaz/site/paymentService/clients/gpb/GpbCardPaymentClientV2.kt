package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.dto.response.GazpromTokenResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardPaymentStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbPayRequestDepr
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbPayCardResponse

@FeignClient(
    name = "gpb-card-payment-client-v2",
    url = "\${api.gpb.card.basePath}",
)
interface GpbCardPaymentClientV2 {
    @PostMapping(value = ["/{portalId}/token"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getToken(
        @PathVariable portalId: String,
    ): GazpromTokenResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun cardPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GpbPayRequestDepr,
    ): GpbPayCardResponse

    @PostMapping(value = ["/{portalId}/payment/{token}/start"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun recurrentPayment(
        @PathVariable portalId: String,
        @PathVariable token: String,
        @RequestBody request: GpbPayRequestDepr,
    ): RegisterCardResponseDto

    @GetMapping(value = ["/{portalId}/payment/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable portalId: String,
        @PathVariable paymentBankId: String,
    ): GpbCardPaymentStatusResponse
}
