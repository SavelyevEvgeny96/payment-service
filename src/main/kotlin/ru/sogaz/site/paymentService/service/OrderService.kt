package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.data.DataOrder
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.siter.models.resonses.Response

interface OrderService {
    /**
     * Метод для создания платежа.
     * Проверяет данные о платеже, валидирует их и создает запись о платеже.
     * @param requestWrapper Данные о заказе(содержит внутри лист PaymentRequest)
     * @return Объект Response с информацией о платеже
     */
    fun createOrder(requestWrapper: OrderRequest): Response<DataOrder>

    fun getOrderStatus(orderId: String): Response<DataGetOrderStatus>
}
