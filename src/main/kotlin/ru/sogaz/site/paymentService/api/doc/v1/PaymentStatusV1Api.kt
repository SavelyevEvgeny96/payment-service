package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.siter.models.resonses.Response

interface PaymentStatusV1Api {
    @Operation(
        summary = "Актуализировать статус оплаты",
        description = "Проверяет статус оплаты и отправляет в очередь (по успешности).",
    )
    @GetMapping("/payment/pay/status/{paymentBankId}")
    fun getStatusPay(
        @PathVariable paymentBankId: String,
    ): Response<ResponseStatusPay>
}
