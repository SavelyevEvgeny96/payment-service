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
    @Column(name = "order_id")
    var orderId: String,
    @Column(name = "code")
    var code: String,
    @ManyToOne
    @JoinColumn(name = "state_id")
    var orderStatus: OrderStatus?,
    @Column(name = "date_delete")
    var dateDelete: String?,
    @ManyToOne
    @JoinColumn(name = "bank_id")
    var bankId: Bank,
    @Column(name = "premium_amount")
    var premiumAmount: Double,
    @Column(name = "payment_end_date")
    var paymentEndDate: String?,
    @Column(name = "url_to_return")
    var urlToReturn: String?,
    @Column(name = "url_to_decline")
    var urlToDecline: String?,
    @Column(name = "custom_url")
    var customURL: String?,
    @Column(name = "pay_id")
    var payId: String,
    @Column(name = "create_date")
    var createDate: String,
    @Column(name = "update_date")
    var updateDate: String,
)
