package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.Data
import ru.sogaz.site.paymentService.dto.PaymentRequest
import ru.sogaz.siter.models.resonses.Response

/**
 * Интерфейс для сервиса обработки платежей.
 * Определяет контракт для работы с платежами.
 */
interface PaymentService {
    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param paymentRequest Данные о платеже
     * @param traceId Идентификатор трассировки
     * @return Объект Response с информацией о платеже
     */
    fun createPayment(
        paymentRequest: PaymentRequest,
        traceId: String,
    ): ResponseEntity<Response<Data>>
}
