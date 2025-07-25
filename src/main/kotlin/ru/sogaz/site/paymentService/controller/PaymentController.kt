
package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentService.dto.DataOrder
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.GpbCallbackRequest
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.site.paymentService.dto.ResponseStatusPay
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
    private val gpbCallbackService: GpbCallbackService
) {
    /**
     * Метод для создания заявки.
     * @param traceId Идентификатор трассировки
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
        @RequestHeader("TraceId") traceId: String,
        @RequestBody requestWrapper: PaymentRequestWrapper,
    ): ResponseEntity<Response<DataOrder>> {
        requestWrapper.payments.forEach { paymentRequest ->
            paymentRequest.traceId = traceId
            paymentRequestValidator.isValid(paymentRequest, requestWrapper)
        }
        return orderService.createOrder(requestWrapper, traceId)
    }

    /**
     * Метод для создания платежа.
     * @return Ответ с кодом состояния и данными о платеже или ошибкой
     */
    @PostMapping("/pay")
    fun createPay(
        @RequestBody paymentPayRequest: PaymentPayRequest,
    ): ResponseEntity<Response<DataPay>> =
        paymentService.createPayment(
            paymentPayRequest,
        )

    @Operation(
        summary = "Проверить статус оплаты",
        description = "Проверяет статус оплаты и отправляет в очередь (по успешности).",
    )
    @GetMapping("/pay/status/{payment_bank_id}")
    fun getStatusPay(
        @RequestParam payment_bank_id: String,
        @RequestHeader traceId: String,
    ): Response<ResponseStatusPay> = paymentStatusCheckerService.getStatus(payment_bank_id, traceId)

    @GetMapping("/gpb/state")
    fun stateGpbCallback(
        @RequestParam("trx_id") trxId: String,
        @RequestParam("merch_id") merchId: String,
        @RequestParam(value = "merchant_trx", required = false) merchantTrx: String?,
        @RequestParam("result_code") resultCode: Int,
        @RequestParam(value = "ext_result_code", required = false) extResultCode: String?,
        @RequestParam("amount") amount: String,
        @RequestParam(value = "account_id", required = false) accountId: String?,
        @RequestParam("o.order_id") orderId: String,
        @RequestParam(value = "p.rrn", required = false) rrn: String?,
        @RequestParam(value = "p.authcode", required = false) authCode: String?,
        @RequestParam(value = "p.srcType", required = false) srcType: String?,
        @RequestParam(value = "signature") signature: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val requestParams = GpbCallbackRequest(
            trxId = trxId,
            merchId = merchId,
            merchantTrx = merchantTrx,
            resultCode = resultCode,
            extResultCode = extResultCode,
            amount = amount,
            accountId = accountId,
            orderId = orderId,
            rrn = rrn,
            authCode = authCode,
            srcType = srcType,
            signature = signature,
            rawQueryString = request.queryString?.substringBefore("&signature=") ?: ""
        )
        return gpbCallbackService.processCallback(requestParams)
    }
}
