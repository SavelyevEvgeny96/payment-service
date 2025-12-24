package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.model.web.request.PayRequest

interface PayV2Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по СБП",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @Parameters(
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            schema = Schema(type = "string"),
            example = "http://www.sogaz.ru",
        ),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты", schema = Schema(type = "string")),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты", schema = Schema(type = "string")),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            schema = Schema(type = "boolean"),
            example = "true",
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по сбп")
    @PostMapping("/v2/payment/paySbp")
    fun paySbp(
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
        @RequestBody
        payRequest: PayRequest,
    ): DataPay

    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @Parameters(
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            schema = Schema(type = "string"),
            example = "http://www.sogaz.ru",
        ),
        Parameter(name = "urlToReturnS", description = "Ссылка для редиректа после успешной оплаты", schema = Schema(type = "string")),
        Parameter(name = "urlToReturnF", description = "Ссылка для редиректа после неуспешной оплаты", schema = Schema(type = "string")),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            schema = Schema(type = "boolean"),
            example = "true",
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @PostMapping("/v2/payment/pay")
    fun pay(
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
        @RequestBody
        payRequest: PayRequest,
    ): DataPay
}
