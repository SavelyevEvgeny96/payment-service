package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.DataPay
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
        urlToReturnS: String? = null,
        urlToReturnF: String? = null,
    ): DataPay

    fun createSBPPayment(
        orderId: UUID,
        urlToReturnS: String? = null,
        urlToReturnF: String? = null,
    ): DataPay

    fun getOrderPaymentPageInfo(orderId: UUID): Response<DataOrderPaymentPageInfo>

    fun updatePaymentInvoice(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): Response<UpdatePaymentInvoiceResponse>

    fun updateStatus(paymentBankId: String): Response<ResponseStatusPay>
}
