package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.response.UpdatePaymentInvoiceResponse
import ru.sogaz.siter.models.resonses.Response
import java.util.UUID

/**
 * Интерфейс для сервиса обработки платежей.
 * Определяет контракт для работы с платежами.
 */
interface PaymentService {
    fun createCardPayment(
        orderId: UUID,
        payQueryParams: PayQueryParams = PayQueryParams(),
    ): DataPay

    fun createSBPPayment(
        orderId: UUID,
        payQueryParams: PayQueryParams = PayQueryParams(),
        paymentDelay: String? = null,
        processPayments: String? = null,
        paymentStatus: String? = null,
    ): DataPay

    fun getOrderPaymentPageInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams = PayQueryParams(),
    ): DataOrderPaymentPageInfo

    fun updatePaymentInvoice(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): Response<UpdatePaymentInvoiceResponse>

    fun updateStatus(paymentBankId: String): ResponseStatusPay
}
