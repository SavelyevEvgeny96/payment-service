package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

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
    var payment: Payment
)