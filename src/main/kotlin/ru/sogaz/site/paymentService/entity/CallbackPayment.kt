package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "callback_payments")
data class CallbackPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "bank_id", nullable = false)
    var bankId: String? = null,
    @Column(name = "type_id", nullable = false)
    var typeId: String? = null,
    @Column(name = "payment_bank_id", nullable = false)
    var paymentBankId: String? = null,
    @Column(name = "qrc_id")
    var qrcId: String? = null,
    @Column(name = "create_date", nullable = false, updatable = false)
    var createDate: LocalDateTime? = null,
    @Column(name = "update_date", nullable = false)
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
