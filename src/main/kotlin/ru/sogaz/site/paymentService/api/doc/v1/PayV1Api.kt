package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import java.util.UUID

interface PayV1Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по СБП",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @Parameters(
        Parameter(name = "orderId", description = "UUID заказа для оплаты", required = true),
        Parameter(name = "urlToReturn", description = "Ссылка для редиректа после успешной оплаты", example = "http://www.sogaz.ru"),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты"),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты"),
        Parameter(name = "depersonalization", description = "Флаг необходимости анонимизированной оплаты", example = "true"),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты картой")
    @GetMapping("/payment/paySbp/{orderId}")
    fun createPaySbp(
        @PathVariable orderId: UUID,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
    ): RedirectView

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
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @GetMapping("/payment/pay/{orderId}")
    fun pay(
        @PathVariable orderId: UUID,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
    ): RedirectView
}
