package ru.sogaz.site.paymentService.dao.v2.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.repository.v2.IdempotentOrderOperationRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Repository
class IdempotentOrderOperationDaoImpl(
    private val idempotentOrderOperationRepository: IdempotentOrderOperationRepository,
) : IdempotentOrderOperationDao {
    override fun findById(operationId: UUID): IdempotentOrderOperation? =
        idempotentOrderOperationRepository.findById(operationId).getOrNull()

    override fun findByOrderIdAndPaymentBankId(
        orderId: UUID?,
        paymentBankId: String,
    ): IdempotentOrderOperation? =
        idempotentOrderOperationRepository.findByIdempotentOrderIdAndPaymentBankIdAndOperationTypeIn(
            orderId,
            paymentBankId,
            listOf(OperationType.PAY, OperationType.RECURRENT, OperationType.REGISTRATION),
        )

    override fun findSucceededByPaymentBankId(paymentBankId: String): IdempotentOrderOperation? =
        idempotentOrderOperationRepository.findFirstByPaymentBankIdAndStateAndOperationTypeInOrderByCreateDateDesc(
            paymentBankId,
            OperationState.SUCCESS,
            listOf(OperationType.PAY, OperationType.RECURRENT, OperationType.REGISTRATION),
        )

    override fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation =
        idempotentOrderOperationRepository.save(idempotentOrderOperation)
}
