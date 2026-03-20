package ru.sogaz.site.paymentService.clients.gpb

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.config.feign.OkHttpClientConfig
import ru.sogaz.site.paymentService.dto.request.GPBQRImageRequest
import ru.sogaz.site.paymentService.dto.request.GPBSBPPaymentRequest
import ru.sogaz.site.paymentService.dto.request.GPBStatusSBPRequest
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.dto.response.GazpromSBPPaymentResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse

@FeignClient(
    name = "gpb-sbp-payment-client",
    url = "\${api.gpb.sbp.basePath}",
    configuration = [OkHttpClientConfig::class],
)
interface GpbSbpPaymentClient {
    @PostMapping(value = ["/qrc-data"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startPayment(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: GPBSBPPaymentRequest,
    ): GazpromSBPPaymentResponse

    @PostMapping(value = ["/qrc-status"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getPaymentStatus(
        @RequestBody request: GPBStatusSBPRequest,
    ): GpbSbpPaymentStatusResponse

    @PostMapping(value = ["/qr-image"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getQrImage(
        @RequestBody request: GPBQRImageRequest,
    ): GPBQRImageResponse
}
