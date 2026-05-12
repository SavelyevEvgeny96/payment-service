package ru.sogaz.site.paymentService.dao.v2

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import java.util.UUID

interface IdempotentOrderDao {
    fun findIdempotentOrderByOrderId(orderId: UUID?): IdempotentOrder?

    fun save(idempotentOrder: IdempotentOrder): IdempotentOrder
}
