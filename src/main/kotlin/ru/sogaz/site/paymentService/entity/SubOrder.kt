package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "sub_orders")
data class SubOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "order_id")
    var order: Order? = null,

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

    @Column(name = "main_contract_check")
    var mainContractCheck: Boolean = false,

    @Column(name = "contract_number")
    var contractNumber: String? = "",

    @Column(name = "premium_amount")
    var premiumAmount: String? = "",

    @Column(name = "policy_date")
    var policyDate: LocalDateTime? = null,

    @Column(name = "contract_date")
    var contractDate: LocalDateTime? = null,

    @Column(name = "manager_email")
    var managerEmail: String? = null,

    @Column(name = "channel")
    var channel: String? = null,

    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
)