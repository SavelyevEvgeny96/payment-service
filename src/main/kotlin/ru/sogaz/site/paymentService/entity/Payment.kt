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
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name =  "state_id")
    var stateId: PaymentStatus?,
    @ManyToOne
    @JoinColumn(name =  "type_id")
    var typeId: PaymentType?,
    @ManyToOne
    @JoinColumn(name = "bank_id")
    var bank: Bank?,
    @ManyToOne
    @JoinColumn(name = "order_id")
    var orderId: Order,
    @Column(name = "payment_started")
    var paymentStarted: String? = null,
    @Column(name = "payment_finished")
    var paymentFinished: String? = null,
    @Column(name = "payment_page_url")
    var paymentPageUrl: String? = null,
    @Column(name = "create_date")
    var createDate: String? = null,
    @Column(name = "update_date")
    var updateDate: String? = null ,
)
