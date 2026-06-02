package ru.sogaz.site.paymentService.model.v2.entity.rules

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "prioritization_rules_banks")
class PrioritizationRulesBanks(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    @Enumerated(EnumType.STRING)
    var bankPriority: OperationBank,
    var bankPriorityCheck: Boolean,
    @Enumerated(EnumType.STRING)
    var bankReserve: OperationBank,
    var partBankPriority: Int,
    var availableGpbCheck: Boolean,
    var availableAbrCheck: Boolean,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
