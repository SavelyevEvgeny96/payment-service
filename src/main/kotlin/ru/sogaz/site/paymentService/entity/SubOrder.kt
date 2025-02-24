package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "sub_orders")
data class SubOrder(
    @Id
    @Column(name = "sub_order_id")
    var subOrderId: String,
    @ManyToOne
    @JoinColumn(name = "order_id")
    var orderId: Order?,
    @Column(name = "operation_id")
    var operationId: String?,
    @Column(name = "manager_email")
    var managerEmail: String?,
    @ManyToOne
    @JoinColumn(name = "external_system_code")
    var clientSystem: ClientSystem?,
    @Column(name = "doc_type")
    var docType: String,
    @Column(name = "policy_id")
    var policyId: String,
    @Column(name = "policy_number")
    var policyNumber: String,
    @Column(name = "contract_id")
    var contractId: String,
    @Column(name = "insurance_program")
    var insuranceProgram: String?,
    @Column(name = "type_insurance")
    var typeInsurance: String?,
    @Column(name = "contract_number")
    var contractNumber: String,
    @Column(name = "hash")
    var hash: String?,
    @Column(name = "premium_amount")
    var premiumAmount: String?,
)
