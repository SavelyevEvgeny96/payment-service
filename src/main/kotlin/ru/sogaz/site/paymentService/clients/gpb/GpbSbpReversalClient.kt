package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.config.feign.OkHttpClientConfig
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp.GpbSbpReversalConfirmRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp.GpbSbpReversalPrepareRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpReversalResponse

@FeignClient(
    name = "gpb-sbp-refund-client",
    url = "\${api.gpb.sbp.basePath}",
    configuration = [OkHttpClientConfig::class],
)
interface GpbSbpReversalClient {
    @PostMapping(value = ["transfer/return/prepare"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun prepare(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: GpbSbpReversalPrepareRequest,
    ): GpbSbpReversalResponse

    @PostMapping(value = ["transfer/return/confirm"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun confirm(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: GpbSbpReversalConfirmRequest,
    ): GpbSbpReversalResponse
}
