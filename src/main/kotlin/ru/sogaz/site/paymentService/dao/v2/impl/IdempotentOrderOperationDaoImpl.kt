package ru.sogaz.site.paymentService.dao.v2.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.repository.v2.IdempotentOrderOperationRepository

@Repository
class IdempotentOrderOperationDaoImpl(
    private val idempotentOrderOperationRepository: IdempotentOrderOperationRepository,
) : IdempotentOrderOperationDao {
    override fun save(idempotentOrderOperation: IdempotentOrderOperation): IdempotentOrderOperation =
        idempotentOrderOperationRepository.save(idempotentOrderOperation)
}
