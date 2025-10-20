package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "cheque_sent")
data class ChequeSent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "payment_bank_id")
    val paymentBankId: String? = null,
    @Column(name = "status")
    val status: String = "",
    @Column(name = "date_create")
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    @Column(name = "date_update")
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
