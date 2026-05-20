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
    @GetMapping("v2/payment/gpb/state", produces = [MediaType.APPLICATION_XML_VALUE])
    fun stateGpbCallback(
        gpbCallback: GpbCardCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<GpbCallbackResponse>

    @PostMapping("v2/payment/sbp/gpb/state")
    fun stateSbpGpbCallback(
        @RequestParam("transactionId") transactionId: String,
        @RequestParam("qrcId") qrcId: String,
        @RequestParam("merchantId") merchantId: String?,
        @RequestParam("amount") amount: String?,
        @RequestParam("currency") currency: String?,
        @RequestParam("dateTime") dateTime: String?,
        @RequestParam("senderId") senderId: String?,
        @RequestParam("senderTypeId") senderTypeId: String?,
        @RequestParam("fpMessageId") fpMessageId: String?,
        @RequestParam("recipientAccountId") recipientAccountId: String?,
        @RequestParam("comment") comment: String?,
        @RequestParam("recipientType") recipientType: String?,
        @RequestParam("fpTransactionType") fpTransactionType: String?,
        @RequestParam("fpTransactionId") fpTransactionId: String?,
        @RequestParam("senderBic") senderBic: String?,
        @RequestParam("recipientInn") recipientInn: String?,
        @RequestParam("timestamp") timestamp: String?,
        @RequestParam("operDate") operDate: String?,
        @RequestParam("status") status: String?,
        request: HttpServletRequest,
    ):ResponseEntity<GpbCallbackResponse>
}
