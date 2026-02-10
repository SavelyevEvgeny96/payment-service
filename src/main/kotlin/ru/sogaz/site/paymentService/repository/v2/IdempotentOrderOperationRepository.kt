package ru.sogaz.site.paymentService.repository.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import java.util.UUID

@Repository
interface IdempotentOrderOperationRepository : JpaRepository<IdempotentOrderOperation, UUID> {
    fun findByIdempotentOrder(idempotentOrder: IdempotentOrder): IdempotentOrderOperation?
}
