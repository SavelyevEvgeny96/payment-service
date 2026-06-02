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
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "rules_banks_products")
class RulesBanksProducts(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var insuranceKind: String,
    var program: String?,
    @Enumerated(EnumType.STRING)
    var bank: OperationBank,
    @Enumerated(EnumType.STRING)
    var paymentType: PaymentType,
    var active: Boolean,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
