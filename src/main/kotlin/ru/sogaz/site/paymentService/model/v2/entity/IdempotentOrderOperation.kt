package ru.sogaz.site.paymentService.model.v2.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.enums.OperationType
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "idempotent_order_operations")
class IdempotentOrderOperation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    @ManyToOne
    var idempotentOrder: IdempotentOrder?,
    var premiumAmount: BigDecimal,
    @Enumerated(EnumType.STRING)
    var bank: OperationBank?,
    var paymentBankId: String?,
    @Enumerated(EnumType.STRING)
    var operationType: OperationType,
    @Enumerated(EnumType.STRING)
    var paymentType: PaymentType,
    @Enumerated(EnumType.STRING)
    var state: OperationState,
    var paymentBankUrl: String?,
    var depersonalization: Boolean,
    var payerIp: String?,
    var description: String?,
    @Type(JsonBinaryType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payItems", columnDefinition = "jsonb")
    var payItems: LinkedHashMap<String, String>?,
    var externalErrorCode: String?,
    var errorText: String?,
    var operationStarted: Instant?,
    var operationFinished: Instant?,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
