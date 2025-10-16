package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import java.util.UUID

interface SubOrderDao {
    fun getSubOrder(
        traceId: String,
        order: Order?,
    ): SubOrder

    fun save(subOrder: SubOrder): SubOrder

    fun saveAll(subOrders: Iterable<SubOrder>): List<SubOrder>

    fun getAllSubOrderListByOrderId(
        orderId: Order,
        traceId: String,
    ): List<SubOrder>?

    fun findAllByOrderId(orderId: UUID): List<SubOrder>
}
