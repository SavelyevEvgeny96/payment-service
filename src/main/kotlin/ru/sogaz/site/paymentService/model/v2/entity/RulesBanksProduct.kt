package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "rules_banks_products")
class RulesBanksProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var insuranceKind: String,
    var program: String,
    var bank: String,
    var paymentType: String,
    var active: Boolean,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
