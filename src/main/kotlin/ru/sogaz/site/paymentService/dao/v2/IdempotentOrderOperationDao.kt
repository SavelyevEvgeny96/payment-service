package ru.sogaz.site.paymentService.dao.v2

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import java.util.UUID

interface IdempotentOrderOperationDao {
    fun findById(operationId: UUID): IdempotentOrderOperation?

    fun findByOrderIdAndPaymentBankId(
        orderId: UUID?,
        paymentBankId: String,
    ): IdempotentOrderOperation?

    fun findSucceededByPaymentBankId(paymentBankId: String): IdempotentOrderOperation?

    fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
