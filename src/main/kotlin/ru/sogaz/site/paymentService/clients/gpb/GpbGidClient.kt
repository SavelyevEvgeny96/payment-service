package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid.GpbGidPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidPayResponse

/**
 * Feign-клиент Газпромбанка для оплаты банковской картой в ГИД.
 */
@FeignClient(
    name = "gpb-gid-client",
    url = "\${api.gpb.gid.basePath}",
)
interface GpbGidClient {
    @PostMapping(value = ["/api/v1/merchant/operation/payment/card"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun pay(
        @RequestHeader("X-Correlation-Id") correlationId: String,
        @RequestBody request: GpbGidPayRequest,
    ): GpbGidPayResponse
}
