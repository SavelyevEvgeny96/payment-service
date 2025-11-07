package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.request.PageInfoRequestParams
import ru.sogaz.site.paymentService.entity.Order
import java.util.UUID

interface InfoPageService {
    fun getInfo(
        orderId: UUID,
        pageInfoRequestParams: PageInfoRequestParams,
    ): DataOrderPaymentPageInfo

    fun getInfo(
        order: Order,
        pageInfoRequestParams: PageInfoRequestParams,
    ): DataOrderPaymentPageInfo
}
