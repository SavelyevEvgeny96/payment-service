package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var paymentId: String,
    @Column(name = "code")
    var code: String,
    @ManyToOne
    @JoinColumn(name = "state_id")
    var orderStatus: OrderStatus?,
    @Column(name = "date_delete")
    var dateDelete: String?,
    @ManyToOne
    @JoinColumn(name = "bank_id",)
    var bank: Bank,
    @Column(name = "operation_id", )
    var operationId: String,
    @Column(name = "payment_end_date")
    var paymentEndDate: String,
    @ManyToOne
    @JoinColumn(name = "external_system_code")
    var clientSystem: ClientSystem,
    @Column(name = "doc_type")
    var docType: String,
    @Column(name = "policy_id")
    var policyId: String,
    @Column(name = "policy_number")
    var policyNumber: String,
    @Column(name = "contract_id")
    var contractId: String,
    @Column(name = "insurance_program")
    var insuranceProgram: String,
    @Column(name = "recipient_user_id")
    var recipientUserId: String,
    @Column(name = "type_insurance")
    var typeInsurance: String,
    @Column(name = "contract_number")
    var contractNumber: String,
    @Column(name = "premium_amount")
    var premiumAmount: String,
    @Column(name = "recipient_email")
    var recipientEmail: String,
    @Column(name = "need_receipt")
    var needReceipt: Boolean,
    @Column(name = "recipient_phone")
    var recipientPhone: String,
    @Column(name = "policyholder")
    var policyholder: String,
    @Column(name = "policyholder_doc")
    var policyholderDoc: String,
    @Column(name = "manager_email")
    var managerEmail: String,
    @Column(name = "url_to_return")
    var urlToReturn: String,
    @Column(name = "url_to_decline")
    var urlToDecline: String?,
    @Column(name = "custom_url")
    var customURL: String,
    @Column(name = "payment_page_url")
    var paymentPageUrl: String?,
    @Column(name = "hash")
    var hash: String,
    @Column(name = "create_date")
    var createDate: String,
    @Column(name = "update_date")
    var updateDate: String,
)
