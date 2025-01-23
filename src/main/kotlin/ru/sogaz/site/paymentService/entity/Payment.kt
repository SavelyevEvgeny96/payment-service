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
    @Column(name = "state_id", nullable = false)
    var stateId: String,
    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    var bank: Bank,
    @Column(name = "payment_started", nullable = false)
    var paymentStarted: String,
    @Column(name = "payment_finished")
    var paymentFinished: String? = null,
    @Column(name = "payment_page_url", nullable = false)
    var paymentPageUrl: String,
    @Column(name = "payment_id", nullable = false)
    var paymentId: String,
    @Column(name = "create_date", nullable = false)
    var createDate: String,
    @Column(name = "update_date", nullable = false)
    var updateDate: String,
)
