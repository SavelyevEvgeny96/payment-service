package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.dto.request.PayQueryParams

@RequestMapping("admin")
interface AdminV1Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @Parameters(
        Parameter(name = "orderId", description = "UUID заказа для оплаты", required = true),
        Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты", example = "http://www.sogaz.ru"),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты"),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты"),
        Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты", example = "true"),
        Parameter(name = "processPayments", description = "Флаг необходимости автоматического проведения оплаты", example = "true"),
        Parameter(name = "paymentDelay", description = "Время задержки для автооплаты", example = "1"),
        Parameter(
            name = "paymentStatus",
            description = "Статус после автооплаты",
            examples = [ExampleObject("PERFORMED"), ExampleObject("ERROR"), ExampleObject("DRAFT")],
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по СБП", useReturnTypeSchema = false)
    @GetMapping("payment/autoclose/paySbp/{orderId}")
    fun createPaySbp(
        @PathVariable orderId: String,
        @Parameter(hidden = true) payQueryParams: PayQueryParams,
        @RequestHeader("processPayments") processPayments: String?,
        @RequestHeader("paymentDelay") paymentDelay: String?,
        @RequestHeader("paymentStatus") paymentStatus: String?,
    ): RedirectView
}
