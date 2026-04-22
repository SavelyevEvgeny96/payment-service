package ru.sogaz.site.paymentService.api.doc.v2

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCallbackResponse
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback

interface GpbCallbackV2Api {
    @GetMapping("v2/payment/gpb/state", produces = [MediaType.APPLICATION_ATOM_XML_VALUE])
    fun stateGpbCallback(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<GpbCallbackResponse>

    @PostMapping("v2/payment/sbp/gpb/state")
    fun stateSbpGpbCallback(
        @RequestParam("qrcId") qrcId: String,
        @RequestParam("merchantId") merchantId: String,
    )
}
