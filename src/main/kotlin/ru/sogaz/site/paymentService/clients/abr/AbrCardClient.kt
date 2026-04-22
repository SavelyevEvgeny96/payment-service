package ru.sogaz.site.paymentService.clients.abr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrOrderResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatusResponse

@FeignClient(
    name = "abr-card-client",
    url = "\${api.abr.card.basePath}",
)
interface AbrCardClient {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun pay(
        @RequestBody request: AbrCardAndSbpPaymentRequest,
    ): AbrOrderResponse

    @GetMapping(value = ["/{paymentBankId}"])
    fun getPaymentStatus(
        @PathVariable paymentBankId: String,
        @RequestParam("password") paymentPass: String,
        @RequestParam("orderDetailLevel") orderDetailLevel: Int = 2,
        @RequestParam("tranDetailLevel") tranDetailLevel: Int = 2,
        @RequestParam("actionDetailLevel") actionDetailLevel: Int = 2,
        @RequestParam("cofpDetailLevel") cofpDetailLevel: Int = 2,
        @RequestParam("consumerDetailLevel") consumerDetailLevel: Int = 2,
        @RequestParam("consumerTokenDetailLevel") consumerTokenDetailLevel: Int = 2,
        @RequestParam("tokenDetailLevel") tokenDetailLevel: Int = 2,
    ): AbrPaymentStatusResponse
}
