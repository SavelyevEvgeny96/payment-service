package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.entity.Order
import java.util.UUID

interface InfoPageService {
    fun getInfo(orderId: UUID): DataOrderPaymentPageInfo

    fun getInfo(order: Order): DataOrderPaymentPageInfo
}
