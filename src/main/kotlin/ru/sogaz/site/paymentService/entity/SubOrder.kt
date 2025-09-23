package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "sub_orders")
data class SubOrder(
    @Id
    @UuidGenerator
    var id: UUID? = null,
    @ManyToOne
    @JoinColumn(name = "order_id")
    var order: Order? = null,
    @Column(name = "operation_id")
    var operationId: String? = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "external_system_code")
    var externalSystemCode: ExternalSystemCodeEnum? = null,
    @Column(name = "doc_type")
    var docType: String? = "",
    @Column(name = "policy_id")
    var policyId: String? = "",
    @Column(name = "policy_number")
    var policyNumber: String? = "",
    @Column(name = "contract_id")
    var contractId: String? = "",
    @Column(name = "insurance_program")
    var insuranceProgram: String? = "",
    @Column(name = "type_insurance")
    var typeInsurance: String? = "",
    @Column(name = "contract_number")
    var contractNumber: String? = "",
    @Column(name = "premium_amount")
    var premiumAmount: String? = "",
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
)
