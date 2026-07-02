package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.config.feign.GpbSbpClientConfig
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbSbpReversalCallback
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbQrImageRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSbpPayRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbSpbStatusRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbQrImageResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayDetailsResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpReversalResponse

@FeignClient(
    name = "gpb-sbp-client",
    url = "\${api.gpb.sbp.basePath}",
    configuration = [GpbSbpClientConfig::class],
)
interface GpbSbpClient {

    @PostMapping(value = ["merchant/qrc-data"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun pay(
        @RequestBody request: GpbSbpPayRequest,
    ): GpbSbpPayResponse

    @PostMapping(value = ["merchant/qrc-status"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getStatus(
        @RequestBody request: GpbSpbStatusRequest,
    ): GpbSbpPayDetailsResponse

    @PostMapping(value = ["operation/info"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getStatusReversal(
        @RequestBody request: GpbSbpReversalCallback,
    ): GpbSbpReversalResponse

    @PostMapping(value = ["merchant/qr-image"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getQrImage(
        @RequestBody request: GpbQrImageRequest,
    ): GpbQrImageResponse
}