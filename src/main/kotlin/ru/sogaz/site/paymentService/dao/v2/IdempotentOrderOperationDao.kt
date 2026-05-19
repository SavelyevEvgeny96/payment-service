package ru.sogaz.site.paymentService.dao.v2

import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import java.util.UUID

interface IdempotentOrderOperationDao {
    fun findById(operationId: UUID): IdempotentOrderOperation?

    fun findByOrderIdAndPaymentBankId(
        orderId: UUID?,
        paymentBankId: String,
    ): IdempotentOrderOperation?

    fun findSucceededByPaymentBankId(paymentBankId: String): IdempotentOrderOperation?

    fun findFirstByPaymentBankIdAndOperationType(
        paymentBankId: String,
        operationType: OperationType,
    ): IdempotentOrderOperation?

    fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation
}
