package ru.sogaz.site.paymentService.repository.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import java.util.UUID

@Repository
interface IdempotentOrderOperationRepository : JpaRepository<IdempotentOrderOperation, UUID> {
    fun findByIdempotentOrderIdAndPaymentBankIdAndOperationTypeIn(
        idempotentOrderId: UUID?,
        paymentBankId: String,
        operationType: List<OperationType>,
    ): IdempotentOrderOperation?

    fun findByQrId(qrId: String): IdempotentOrderOperation?

    fun findFirstByPaymentBankIdAndStateAndOperationTypeInOrderByCreateDateDesc(
        paymentBankId: String,
        state: OperationState,
        operationType: List<OperationType>,
    ): IdempotentOrderOperation?

    fun findFirstByIdempotentOrderIdAndStateAndOperationTypeInOrderByCreateDateDesc(
        idempotentOrderId: UUID,
        state: OperationState,
        operationType: List<OperationType>,
    ): IdempotentOrderOperation?
}
