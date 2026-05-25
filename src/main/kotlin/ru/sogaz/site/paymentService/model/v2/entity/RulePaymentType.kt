package ru.sogaz.site.paymentService.model.v2.entity

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
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import java.time.Instant
import java.util.UUID

/**
 * Сущность правила доступности конкретной операции для типа оплаты и банка.
 */
@Entity
@Table(name = "rules_payment_type")
class RulePaymentType(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    @Enumerated(EnumType.STRING)
    var bank: OperationBank,
    @Enumerated(EnumType.STRING)
    var paymentType: PaymentType,
    @Enumerated(EnumType.STRING)
    var operationType: OperationType,
    var availability: Boolean,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
