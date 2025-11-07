package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_FORBIDDEN_PAYMENT_INVOICE
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.response.UpdatePaymentInvoiceResponse
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.validation.PermissionValidator
import ru.sogaz.siter.models.resonses.Response

/**
 * Контроллер для обработки запросов на создание платежа.
 * Обрабатывает POST запросы для создания ссылки на оплату.
 */
@RestController
@Tag(name = "Payment", description = "Работа с платежами")
class PaymentController(
    private val paymentService: PaymentService,
    private val permissionValidator: PermissionValidator,
) {
    @Operation(
        summary = "Проверить статус оплаты",
        description = "Проверяет статус оплаты и отправляет в очередь (по успешности).",
    )
    @GetMapping("/payment/pay/status/{paymentBankId}")
    fun getStatusPay(
        @PathVariable paymentBankId: String,
    ): Response<ResponseStatusPay> = paymentService.updateStatus(paymentBankId)

    @PatchMapping("/payment/paymentinvoice")
    fun updatePaymentInvoice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestBody updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
    ): Response<UpdatePaymentInvoiceResponse> {
        permissionValidator.checkPermission(authorization, CODE_ERROR_FORBIDDEN_PAYMENT_INVOICE)
        return paymentService.updatePaymentInvoice(updatePaymentInvoiceRequest)
    }
}
