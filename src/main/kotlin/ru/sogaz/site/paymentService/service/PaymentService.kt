package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.siter.models.resonses.Response

/**
 * Интерфейс для сервиса обработки платежей.
 * Определяет контракт для работы с платежами.
 */
interface PaymentService {
    fun createPayment(
        request: PaymentPayRequest,
        traceId: String,
    ): ResponseEntity<Response<DataPay>>

    fun initiateGPBPayment(paymentPayRequest: PaymentPayRequest, traceId: String,tokenGpb:String): ResponseEntity<Response<DataPay>>
    fun getGPBToken(traceId: String): String
}
