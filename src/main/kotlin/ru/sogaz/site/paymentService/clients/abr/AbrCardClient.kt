package ru.sogaz.site.paymentService.clients.abr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.dto.request.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.dto.response.AbrOrderResponse
import ru.sogaz.site.paymentService.dto.response.PaymentAbrStatusResponse

@FeignClient(
    name = "abr-card-client",
    url = "\${api.payment.abrUrl}",
)
interface AbrCardClient {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun cardPayment(
        @RequestBody request: AbrCardAndSbpPaymentRequest,
    ): AbrOrderResponse

    @GetMapping("/{paymentBankId}")
    fun getPaymentStatus(
        @PathVariable("paymentBankId") paymentBankId: String?,
        @RequestParam("password") password: String?,
        @RequestParam("orderDetailLevel") orderDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("tranDetailLevel") tranDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("actionDetailLevel") actionDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("cofpDetailLevel") cofpDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("consumerDetailLevel") consumerDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("consumerTokenDetailLevel") consumerTokenDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
        @RequestParam("tokenDetailLevel") tokenDetailLevel: Int = DEFAULT_DETAIL_LEVEL,
    ): PaymentAbrStatusResponse

    companion object {
        private const val DEFAULT_DETAIL_LEVEL = 2
    }
}
