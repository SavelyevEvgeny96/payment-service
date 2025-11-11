package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import java.util.UUID

interface InfoPageService {
    fun getInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo

    fun getInfo(
        order: Order,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo
}
