package ru.sogaz.site.paymentService.repository.v2

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import java.util.Optional
import java.util.UUID

@Repository
interface IdempotentOrderRepository : JpaRepository<IdempotentOrder, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findById(id: UUID?): Optional<IdempotentOrder>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun save(idempotentOrder: IdempotentOrder): IdempotentOrder
}
