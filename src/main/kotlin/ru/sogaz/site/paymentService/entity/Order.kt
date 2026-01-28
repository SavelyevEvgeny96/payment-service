package ru.sogaz.site.paymentService.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderStatus = OrderStatus.NEW,
    @Enumerated(EnumType.STRING)
    @Column(name = "bank")
    var bank: BankEnum? = null,
    @Column(name = "premium_amount")
    var premiumAmount: String = "",
    @Column(name = "recipient_email")
    var recipientEmail: String? = "",
    @Column(name = "url_to_return")
    var urlToReturn: String? = "",
    @Column(name = "url_to_decline")
    var urlToDecline: String? = "",
    @Column(name = "payment_end_date")
    var paymentEndDate: LocalDateTime? = null,
    @Column(name = "date_delete")
    var dateDelete: LocalDateTime? = null,
    @Column(name = "recipient_phone")
    var recipientPhone: String? = null,
    @Column(name = "recipient_user_id")
    var recipientUserId: String? = null,
    @Column(name = "policyholder")
    var policyholder: String? = null,
    @Column(name = "unified_id")
    var unifiedId: String? = null,
    @Column(name = "key_card")
    var keyCard: String? = null,
    @Column(name = "save_card")
    var saveCard: Boolean = false,
    @Column(name = "reg_card")
    var regCard: Boolean = false,
    @Column(name = "subscription_id")
    var subscriptionId: String = "",
    @Column(name = "client_id")
    var clientId: String,
    @Column(name = "recurrent")
    var recurrent: Boolean? = null,
    @Column(name = "order_id_recurrent")
    var orderIdRecurrent: UUID? = null,
    @Column(name = "queue_status_result_name")
    var queueStatusResultName: String? = null,
    @Column(name = "skip_sending_queue")
    var skipSendingQueue: Boolean? = null,
    @Column(name = "skip_sending_receipt")
    var skipSendingReceipt: Boolean? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
) {
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER, mappedBy = "order")
    @Fetch(FetchMode.SUBSELECT)
    val subOrders: MutableList<SubOrder> = mutableListOf()

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "order")
    @Fetch(FetchMode.SUBSELECT)
    val payments: MutableList<Payment> = mutableListOf()
}
