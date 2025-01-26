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
    var paymentId: String? = null,
    @Column(name = "code", nullable = false)
    var code: String,
    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    var orderStatus: OrderStatus?,
    @Column(name = "date_delete")
    var dateDelete: String? = null,
    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    var bank: Bank,
    @Column(name = "operation_id", nullable = false)
    var operationId: String,
    @Column(name = "payment_end_date")
    var paymentEndDate: String? = null,
    @ManyToOne
    @JoinColumn(name = "external_system_code", nullable = false)
    var clientSystem: ClientSystem?,
    @Column(name = "doc_type", nullable = false)
    var docType: String,
    @Column(name = "policy_id", nullable = false)
    var policyId: String,
    @Column(name = "policy_number", nullable = false)
    var policyNumber: String,
    @Column(name = "contract_id", nullable = false)
    var contractId: String?,
    @Column(name = "premium_amount", nullable = false)
    var premiumAmount: String?,
    @Column(name = "recipient_email", nullable = false)
    var recipientEmail: String,
    @Column(name = "need_receipt", nullable = false)
    var needReceipt: Boolean?,
    @Column(name = "recipient_phone", nullable = false)
    var recipientPhone: String,
    @Column(name = "policyholder", nullable = false)
    var policyholder: String,
    @Column(name = "policyholder_doc", nullable = false)
    var policyholderDoc: String,
    @Column(name = "manager_email", nullable = false)
    var managerEmail: String,
    @Column(name = "url_to_return", nullable = false)
    var urlToReturn: String,
    @Column(name = "url_to_decline", nullable = false)
    var urlToDecline: String,
    @Column(name = "custom_url", nullable = false)
    var customURL: String,
    @Column(name = "payment_page_url", nullable = false)
    var paymentPageUrl: String,
    @Column(name = "hash", nullable = false)
    var hash: String,
    @Column(name = "create_date", nullable = false)
    var createDate: String,
    @Column(name = "update_date", nullable = false)
    var updateDate: String,
)
