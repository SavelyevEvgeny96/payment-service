package ru.sogaz.site.paymentService.entity

import jakarta.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "order_id")
    var orderId: String,
    @Column(name = "code")
    var code: String,
    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName ="state_id")
    var orderStatus: OrderStatus?,
    @ManyToOne
    @JoinColumn(name = "bank_id", referencedColumnName = "bank_id")
    var bankId: Bank,
    @Column(name = "date_delete")
    var dateDelete: String?,
    @Column(name = "premium_amount")
    var premiumAmount: String?,
    @Column(name = "payment_end_date")
    var paymentEndDate: String?,
    @Column(name = "url_to_return")
    var urlToReturn: String?,
    @Column(name = "url_to_decline")
    var urlToDecline: String?,
    @Column(name = "custom_url")
    var customURL: String?,
    @Column(name = "create_date")
    var createDate: String,
    @Column(name = "update_date")
    var updateDate: String,
    @Column(name = "recipient_email")
    var recipientEmail: String,
    @Column(name = "need_receipt")
    var needReceipt: Boolean?,
    @Column(name = "recipient_phone")
    var recipientPhone: String?,
    @Column(name = "policyholder")
    var policyholder: String?,
    @Column(name = "policyholder_doc")
    var policyholderDoc: String?,
    @Column(name = "recipient_user_id")
    var recipientUserId: String?,
)
