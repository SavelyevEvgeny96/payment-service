package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.model.web.request.PayRequest

@RequestMapping("admin")
interface AdminV2Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @Parameters(
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            example = "http://www.sogaz.ru",
            schema = Schema(type = "string"),
        ),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты", schema = Schema(type = "string")),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты", schema = Schema(type = "string")),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            example = "true",
            schema = Schema(type = "boolean"),
        ),
        Parameter(name = "processPayments", description = "Флаг необходимости автоматического проведения оплаты", example = "true"),
        Parameter(name = "paymentDelay", description = "Время задержки для автооплаты", example = "1"),
        Parameter(
            name = "paymentStatus",
            description = "Статус после автооплаты",
            example = "PERFORMED",
            examples = [ExampleObject(value = "PERFORMED"), ExampleObject(value = "ERROR"), ExampleObject(value = "DRAFT")],
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по СБП", useReturnTypeSchema = false)
    @PostMapping("/v2/payment/autoclose/paySbp")
    fun createPaySbp(
        @RequestHeader("processPayments") processPayments: String?,
        @RequestHeader("paymentDelay") paymentDelay: String?,
        @RequestHeader("paymentStatus") paymentStatus: String?,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
        @RequestBody
        payRequest: PayRequest,
    ): DataPay
}
