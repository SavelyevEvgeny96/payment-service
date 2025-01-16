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
@Table(name = "payment_operation_history")
class PaymentOperationHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "action", nullable = false)
    var action: String,
    @Column(name = "action_date", nullable = false)
    var actionDate: String,
    @Column(name = "action_author", nullable = false)
    var actionAuthor: String,
    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    var payment: Payment,
)
