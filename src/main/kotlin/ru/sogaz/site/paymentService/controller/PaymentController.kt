package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.CallbackRequest
import ru.sogaz.site.paymentService.dto.request.GpbCallbackRequest
import ru.sogaz.site.paymentService.dto.request.PaymentRequestWrapper
import ru.sogaz.site.paymentService.dto.response.CallbackResponse
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.service.CallbackService
import ru.sogaz.site.paymentService.service.GpbCallbackService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.site.paymentService.validation.PaymentRequestValidator
import ru.sogaz.siter.models.resonses.Response

/**
 * Контроллер для обработки запросов на создание платежа.
 * Обрабатывает POST запросы для создания ссылки на оплату.
 */
@RestController
@RequestMapping("/payment")
class PaymentController(
    private val orderService: OrderService,
    private val paymentService: PaymentService,
    private val paymentStatusCheckerService: PaymentStatusCheckerService,
    private val paymentRequestValidator: PaymentRequestValidator,
    private val gpbCallbackService: GpbCallbackService,
    private val callbackService: CallbackService,
) {
    /**
     * Метод для создания заявки.
     * getTraceId() Идентификатор трассировки
     * @return Ответ с кодом состояния и данными о платеже или ошибкой
     */
    @Operation(
        summary = "Создать заявку на оплату",
        description = "Создает заявку и возвращает ссылку на оплату.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешное создание платежа",
                content = [Content(schema = Schema(implementation = DataOrder::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Неавторизованный запрос",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500401," +
                                        " \"messageError\": \"Ваш запрос не авторизован\"}",
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Доступ запрещен",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500403, " +
                                        "\"messageError\": \"Вам запрещен доступ к запрашиваемому ресурсу\"}",
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Ошибка валидации данных",
                content = [
                    Content(
                        schema =
                            Schema(
                                example =
                                    "{\"status\": \"error\", \"code\": -1101500422, " +
                                        "\"messageError\": \"Не все обязательные данные указаны корректно\"}",
                            ),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/create")
    fun createOrder(
        @RequestBody requestWrapper: PaymentRequestWrapper,
    ): ResponseEntity<Response<DataOrder>> {
        requestWrapper.payments.forEach { paymentRequest ->
            paymentRequestValidator.isValid(paymentRequest, requestWrapper, getTraceId())
        }
        return orderService.createOrder(requestWrapper)
    }

    @GetMapping("/paySbp/{orderId}")
    fun createPaySbp(
        @PathVariable orderId: String,
        @RequestParam(required = false) urlToReturn: String?,
        @RequestParam(required = false) urlToReturnF: String?,
    ): ResponseEntity<Response<DataPay>> = paymentService.createPaymentSbp(urlToReturn, urlToReturnF, orderId)

    @GetMapping("/pay/{orderId}")
    fun pay(
        @PathVariable orderId: String,
        @RequestParam(required = false) urlToReturn: String?,
        @RequestParam(required = false) urlToReturnF: String?,
    ): ResponseEntity<Response<DataPay>> = paymentService.createPayment(urlToReturn, urlToReturnF, orderId)

    @Operation(
        summary = "Проверить статус оплаты",
        description = "Проверяет статус оплаты и отправляет в очередь (по успешности).",
    )
    @GetMapping("/pay/status/{payment_bank_id}")
    fun getStatusPay(
        @PathVariable payment_bank_id: String,
    ): Response<ResponseStatusPay> = paymentStatusCheckerService.getStatus(payment_bank_id)

    @GetMapping("/order/status/{orderId}")
    fun getOrderStatus(
        @PathVariable orderId: String,
    ): Response<DataGetOrderStatus> = orderService.getOrderStatus(orderId)

    @GetMapping("/gpb/state")
    fun stateGpbCallback(
        @RequestParam("trx_id") trxId: String,
        @RequestParam("merch_id") merchId: String?,
        @RequestParam("result_code") resultCode: Int?,
        @RequestParam("amount") amount: String?,
        @RequestParam(value = "account_id", required = false) accountId: String?,
        @RequestParam("o.order_id") orderId: String?,
        @RequestParam(value = "p.rrn", required = false) rrn: String?,
        @RequestParam(value = "p.authcode", required = false) authCode: String?,
        @RequestParam(value = "p.srcType", required = false) srcType: String?,
        @RequestParam(value = "p.maskedPan", required = false) maskedPan: String?,
        @RequestParam(value = "p.isFullyAuthenticated", required = false) isFullyAuthenticated: String?,
        @RequestParam(value = "p.transmissionDateTime", required = false) transmissionDateTime: String?,
        @RequestParam("discountType") discountType: String?,
        @RequestParam("discountAmount") discountAmount: String?,
        @RequestParam(value = "p.paymentSystem", required = false) paymentSystem: String?,
        @RequestParam(value = "p.issuerName", required = false) issuerName: String?,
        @RequestParam("ts") ts: String?,
        @RequestParam(value = "signature") signature: String,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        val requestParams =
            GpbCallbackRequest(
                trxId,
                merchId,
                resultCode,
                amount,
                accountId,
                orderId,
                rrn,
                authCode,
                srcType,
                maskedPan,
                isFullyAuthenticated,
                transmissionDateTime,
                discountType,
                discountAmount,
                paymentSystem,
                issuerName,
                ts,
                signature,
            )
        return gpbCallbackService.processCallback(requestParams)
    }

    @PostMapping("/akb/state")
    fun stateRussiaCallback(
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

    @GetMapping("/sbp/gpb/state")
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
    ): Response<CallbackResponse> {
        val requestParams =
            CallbackRequest(
                bankId = transactionId,
            )
        return callbackService.processCallback(requestParams)
    }
}
