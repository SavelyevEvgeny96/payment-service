package ru.sogaz.site.paymentService.clients.abr

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrCardAndSbpPaymentRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrPreparePushTranRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.abr.AbrSetSrcTokenRequest
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrOrderResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPreparePushTranResponse

@FeignClient(
    name = "abr-sbp-client",
    url = "\${api.abr.sbp.basePath}",
)
interface AbrSbpClient {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun pay(
        @RequestBody request: AbrCardAndSbpPaymentRequest,
    ): AbrOrderResponse

    @PostMapping(value = ["/{paymentBankId}/set-src-token"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun setSrcToken(
        @PathVariable paymentBankId: String,
        @RequestParam("password") paymentPass: String,
        @RequestBody request: AbrSetSrcTokenRequest,
    )

    @PostMapping(value = ["/{paymentBankId}/prepare-push-tran"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun preparePushTran(
        @PathVariable paymentBankId: String,
        @RequestParam("password") paymentPass: String,
        @RequestBody request: AbrPreparePushTranRequest,
    ): AbrPreparePushTranResponse
}
