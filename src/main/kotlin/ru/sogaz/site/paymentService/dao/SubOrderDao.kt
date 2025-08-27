package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder

interface SubOrderDao {
    fun getSubOrder(
        traceId: String,
        order: Order?,
    ): SubOrder
    fun save(subOrder: SubOrder)
    fun getAllSubOrderListByOrderId(orderId: Order, traceId: String): List<SubOrder>
}
