package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.model.v2.enums.payment.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.payment.PaymentType
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "idempotent_order_operations")
class IdempotentOrderOperation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    @ManyToOne
    @JoinColumn(name = "order_id")
    var idempotentOrder: IdempotentOrder,
    @Enumerated(EnumType.STRING)
    var bank: BankEnum?,
    var paymentBankId: String?,
    @Enumerated(EnumType.STRING)
    var operationType: OperationType,
    @Enumerated(EnumType.STRING)
    var paymentType: PaymentType,
    @Enumerated(EnumType.STRING)
    var state: PaymentStatusEnum,
    var paymentBankUrl: String?,
    var depersonalization: Boolean = false,
    @CreationTimestamp
    var operationStarted: LocalDateTime? = null,
    @UpdateTimestamp
    var operationFinished: LocalDateTime? = null,
)
