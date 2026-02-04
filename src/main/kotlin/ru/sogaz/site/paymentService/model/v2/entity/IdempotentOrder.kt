package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "idempotent_orders")
data class IdempotentOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var orderId: UUID,
) : Serializable
