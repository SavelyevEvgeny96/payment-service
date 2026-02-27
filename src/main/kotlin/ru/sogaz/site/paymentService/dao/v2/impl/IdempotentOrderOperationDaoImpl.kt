package ru.sogaz.site.paymentService.dao.v2.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
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
        orderId: UUID,
        paymentBankId: String,
    ): IdempotentOrderOperation? = idempotentOrderOperationRepository.findByIdempotentOrderIdAndPaymentBankId(orderId, paymentBankId)

    override fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation =
        idempotentOrderOperationRepository.save(idempotentOrderOperation)
}
