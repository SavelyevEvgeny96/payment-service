package ru.sogaz.site.paymentService.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_FORBIDDEN_PAYMENT_INVOICE
import ru.sogaz.site.paymentService.api.doc.v1.PaymentInvoiceV1Api
import ru.sogaz.site.paymentService.api.doc.v1.PaymentStatusV1Api
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.response.UpdatePaymentInvoiceResponse
import ru.sogaz.site.paymentService.properties.ServiceStatuses.Companion.SUCCESS_STATUS_CODE_UPDATE_PAYMENT_STATUS
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
) : WrapResponseController(),
    PaymentStatusV1Api,
    PaymentInvoiceV1Api {
    override fun getStatusPay(
        @PathVariable paymentBankId: String,
    ): Response<ResponseStatusPay> =
        paymentService
            .updateStatus(paymentBankId)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_UPDATE_PAYMENT_STATUS)

    override fun updatePaymentInvoice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestBody updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
    ): Response<UpdatePaymentInvoiceResponse> {
        permissionValidator.checkPermission(authorization, CODE_ERROR_FORBIDDEN_PAYMENT_INVOICE)
        return paymentService.updatePaymentInvoice(updatePaymentInvoiceRequest)
    }
}
