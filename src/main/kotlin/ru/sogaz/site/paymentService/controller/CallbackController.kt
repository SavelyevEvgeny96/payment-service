package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.api.doc.v1.CallbackV1Api
import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.dto.response.CallbackResponse
import ru.sogaz.site.paymentService.service.CallbackService
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.siter.models.resonses.Response

@RestController
@Tag(name = "Callback", description = "Прием callback-ов от банков")
class CallbackController(
    private val gpbCallbackService: GpbCallbackService,
    private val callbackService: CallbackService,
) : CallbackV1Api {
    override fun stateGpbCallback(
        gpbCallback: GpbCallback,
        httpServletRequest: HttpServletRequest,
    ): ResponseEntity<String> {
        println(gpbCallback)
        return gpbCallbackService.processCallback(gpbCallback, httpServletRequest)
    }

    override fun stateRussiaCallback(
        @RequestParam("ORDER_ID") orderId: String,
        @RequestParam("ORDER_RID") orderRid: String?,
        @RequestParam("ORDER_STATUS") orderStatus: String?,
        @RequestParam("ORDER_STORED_TOKENS_IDS") orderToken: String?,
        @RequestParam(value = "pmoResultCode", required = false) pmoResultCode: String?,
        @RequestParam(value = "ridByPmo", required = false) ridByPmo: String?,
        request: HttpServletRequest,
    ): Response<CallbackResponse> {
        val requestParams =
            CallbackRequest(
                bankId = orderId,
            )
        return callbackService.processCallback(requestParams)
    }

    override fun stateSbpGpbCallback(
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
    ): Response<CallbackResponse> {
        val requestParams =
            CallbackRequest(
                bankId = transactionId,
            )
        return callbackService.processCallback(requestParams)
    }
}
