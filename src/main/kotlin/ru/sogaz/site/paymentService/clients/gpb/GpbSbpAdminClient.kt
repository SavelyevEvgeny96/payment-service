package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse

@FeignClient(
    name = "gpb-sbp-admin-client",
    url = "\${api.gpb.sbp.basePath}",
)
@Profile(value = ["local", "test", "stage"])
interface GpbSbpAdminClient {
    @PostMapping(value = ["/qrc-data"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun autoPay(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: GpbSbpPayRequest,
    ): GpbSbpPayResponse
}
