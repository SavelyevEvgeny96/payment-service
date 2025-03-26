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
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "state_id")
    var stateId: PaymentStatus?,
    @ManyToOne
    @JoinColumn(name = "type_id", referencedColumnName = "type_id")
    var typeId: PaymentType?,
    @ManyToOne
    @JoinColumn(name = "bank_id", referencedColumnName = "bank_id")
    var bank: Bank?,
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    var orderId: Order,
    @Column(name = "payment_bank_id")
    var paymentBankId: String? = null,
    @Column(name = "payment_started")
    var paymentStarted: String? = null,
    @Column(name = "payment_finished")
    var paymentFinished: String? = null,
    @Column(name = "payment_page_url")
    var paymentPageUrl: String? = null,
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
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
