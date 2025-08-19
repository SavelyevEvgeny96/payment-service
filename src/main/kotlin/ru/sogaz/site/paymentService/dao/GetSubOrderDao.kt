package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder

interface GetSubOrderDao {
    fun getSubOrder(
        traceId: String,
        order: Order?,
    ): SubOrder
}
