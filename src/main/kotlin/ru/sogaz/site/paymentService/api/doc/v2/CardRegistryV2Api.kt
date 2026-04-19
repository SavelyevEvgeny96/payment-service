package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.siter.models.resonses.Response

interface CardRegistryV2Api {
    @Operation(
        summary = "Регистрация банковской карты",
        description = "Создание операции регистрации банковской карты",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = BankPaymentPageData::class),
            ),
        ],
        description = "Данные для перехода на страницу банка",
    )
    @PostMapping("/v2/payment/regcard")
    fun cardRegistry(
        @RequestBody
        request: PayRegOperationRequest,
    ): Response<BankPaymentPageData>
}
