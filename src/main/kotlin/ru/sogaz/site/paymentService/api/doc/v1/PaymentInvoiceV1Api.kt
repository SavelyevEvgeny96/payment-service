package ru.sogaz.site.paymentService.api.doc.v1

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.dto.response.UpdatePaymentInvoiceResponse
import ru.sogaz.siter.models.resonses.Response

interface PaymentInvoiceV1Api {
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/payment/paymentinvoice")
    fun updatePaymentInvoice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestBody updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
    ): Response<UpdatePaymentInvoiceResponse>
}
