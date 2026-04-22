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
@Table(name = "prioritization_rules_banks")
class PrioritizationRulesBank(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var bankPriority: String,
    var bankPriorityCheck: Boolean,
    var bankReserve: String,
    var partBankPriority: Int,
    var availableGpbCheck: Boolean,
    var availableAbrCheck: Boolean,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
