package ru.sogaz.site.paymentService.api.doc.v1

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.dto.response.CallbackResponse
import ru.sogaz.siter.models.resonses.Response

interface CallbackV1Api {
    @GetMapping("payment/gpb/state")
    fun stateGpbCallback(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String>

    @PostMapping("payment/akb/state")
    fun stateRussiaCallback(
        @RequestParam("ORDER_ID") orderId: String,
        @RequestParam("ORDER_RID") orderRid: String?,
        @RequestParam("ORDER_STATUS") orderStatus: String?,
        @RequestParam("ORDER_STORED_TOKENS_IDS") orderToken: String?,
        @RequestParam(value = "pmoResultCode", required = false) pmoResultCode: String?,
        @RequestParam(value = "ridByPmo", required = false) ridByPmo: String?,
        request: HttpServletRequest,
    ): Response<CallbackResponse>

    @PostMapping("payment/sbp/gpb/state")
    fun stateSbpGpbCallback(
        @RequestParam("transactionId") transactionId: String,
        @RequestParam("qrcId") qrcId: String?,
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
    ): Response<CallbackResponse>
}
