package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.config.OkHttpClientConfig
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSpbStatusRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse

@FeignClient(
    name = "gpb-sbp-client",
    url = "\${api.gpb.sbp.basePath}",
    configuration = [OkHttpClientConfig::class],
)
interface GpbSbpClient {
    @PostMapping(value = ["/qrc-data"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun pay(
        @RequestBody request: GpbSbpPayRequest,
    ): GpbSbpPayResponse

    @PostMapping(value = ["/qrc-status"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getStatus(
        @RequestBody request: GpbSpbStatusRequest,
    ): GpbSbpPayDetailsResponse
}
