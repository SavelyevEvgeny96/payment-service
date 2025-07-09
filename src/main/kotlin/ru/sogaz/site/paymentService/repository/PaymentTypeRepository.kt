package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.PaymentType

@Repository
interface PaymentTypeRepository : JpaRepository<PaymentType, Long> {
    fun findByTypeId(typeId: String): PaymentType
}
