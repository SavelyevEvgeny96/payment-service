package ru.sogaz.site.paymentService.dao.v2

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

interface IdempotentOrderOperationDao {
    fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
