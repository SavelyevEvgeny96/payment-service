package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.data.PaymentContext
import ru.sogaz.siter.models.resonses.Response

/**
 * Интерфейс для сервиса обработки платежей.
 * Определяет контракт для работы с платежами.
 */
interface PaymentService {
    fun createPayment(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
    ): ResponseEntity<Response<DataPay>>

    fun createPaymentSbp(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
    ): ResponseEntity<Response<DataPay>>

    fun buildPaymentContext(
        urlToReturn: String?,
        urlToReturnF: String?,
        orderId: String,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
    ): PaymentContext
}
