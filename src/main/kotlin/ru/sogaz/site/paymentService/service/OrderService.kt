package ru.sogaz.site.paymentService.service

import org.springframework.http.ResponseEntity
import ru.sogaz.site.paymentService.dto.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.DataOrder
import ru.sogaz.site.paymentService.dto.PaymentRequestWrapper
import ru.sogaz.siter.models.resonses.Response

interface OrderService {
    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param requestWrapper Данные о заказе(содержит внутри лист PaymentRequest)
     * @return Объект Response с информацией о платеже
     */
    fun createOrder(requestWrapper: PaymentRequestWrapper): ResponseEntity<Response<DataOrder>>

    fun getOrderStatus(orderId: String): Response<DataGetOrderStatus>
}
