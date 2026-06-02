package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "idempotent_orders")
data class IdempotentOrder(
    @Id
    var id: UUID,
)
