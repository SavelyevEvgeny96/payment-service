package ru.sogaz.site.paymentService.dao.v2.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.repository.v2.IdempotentOrderRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Repository
class IdempotentOrderDaoImpl(
    private val idempotentOrderRepository: IdempotentOrderRepository,
) : IdempotentOrderDao {
    override fun findIdempotentOrderByOrderId(orderId: UUID): IdempotentOrder? = idempotentOrderRepository.findById(orderId).getOrNull()

    override fun save(idempotentOrder: IdempotentOrder): IdempotentOrder = idempotentOrderRepository.save(idempotentOrder)
}
