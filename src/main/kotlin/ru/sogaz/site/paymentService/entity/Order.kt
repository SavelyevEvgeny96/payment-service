package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "order_id")
    var orderId: String? = "",
    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "state_id")
    var orderStatus: OrderStatus? = null,
    @ManyToOne
    @JoinColumn(name = "bank_id", referencedColumnName = "bank_id")
    var bankId: Bank? = null,
    @Column(name = "date_delete")
    var dateDelete: String? = "",
    @Column(name = "premium_amount")
    var premiumAmount: String = "",
    @Column(name = "payment_end_date")
    var paymentEndDate: String? = "",
    @Column(name = "url_to_return")
    var urlToReturn: String? = "",
    @Column(name = "url_to_decline")
    var urlToDecline: String? = "",
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
    @Column(name = "recipient_email")
    var recipientEmail: String? = "",
) {
    @PrePersist
    fun prePersist() {
        createDate = LocalDateTime.now()
        updateDate = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updateDate = LocalDateTime.now()
    }
}
