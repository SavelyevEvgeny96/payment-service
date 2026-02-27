package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams

interface CardRegistryV2Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрация банковской карты",
    )
    @Parameters(
        Parameter(
            name = "unifiedId",
            description = "Идентификатор единого профиля клиента",
            schema = Schema(type = "string"),
            required = true,
        ),
        Parameter(
            name = "urlToReturn",
            description = "Ссылка для редиректа после успешной оплаты",
            example = "http://www.sogaz.ru",
            schema = Schema(type = "string"),
            required = true,
        ),
        Parameter(
            name = "urlToReturnS",
            description = "Ссылка для редиректа после успешной оплаты",
            schema = Schema(type = "string"),
            required = true,
        ),
        Parameter(
            name = "urlToReturnF",
            description = "Ссылка для редиректа после неуспешной оплаты",
            schema = Schema(type = "string"),
            required = true,
        ),
        Parameter(
            name = "depersonalization",
            description = "Флаг необходимости анонимизированной оплаты",
            schema = Schema(type = "boolean"),
            example = "true",
        ),
    )
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/v2/payment/users/{unifiedId}/card")
    fun cardRegistry(
        @PathVariable unifiedId: String,
        @Parameter(hidden = true)
        payQueryParams: PayQueryParams,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) token: String,
    ): DataPay
}
