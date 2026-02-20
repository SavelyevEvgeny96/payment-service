package ru.sogaz.site.paymentService.api.doc.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.sogaz.site.paymentService.api.doc.response.ValidationErrorApiResponse
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.siter.models.resonses.Response

interface PayV2Api {
    @Operation(
        summary = "Редирект на страницу оплаты заказа по карте",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @ValidationErrorApiResponse
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по карте")
    @PostMapping("/v2/payment/pay")
    fun pay(
        @RequestBody cardPayOperationRequest: CardPayOperationRequest,
    ): Response<BankPaymentPageData>

    @Operation(
        summary = "Редирект на страницу оплаты заказа по СБП",
        description = "Регистрирует платеж в банке указанном для заказа и перенаправляет на платежную страницу банка",
    )
    @ValidationErrorApiResponse
    @ApiResponse(responseCode = "200", description = "Редирект на страницу оплаты по сбп")
    @PostMapping("/v2/payment/paySbp")
    fun paySbp(
        @RequestBody sbpPayOperationRequest: SbpPayOperationRequest,
    ): Response<BankPaymentPageData>
}
