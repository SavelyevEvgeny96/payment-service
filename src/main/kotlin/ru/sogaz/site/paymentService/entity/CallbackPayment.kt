package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import java.time.LocalDateTime

@Entity
@Table(name = "callback_payments")
data class CallbackPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "bank")
    var bank: BankEnum,
    @Column(name = "type")
    var type: PaymentTypeEnum,
    @Column(name = "payment_bank_id")
    var paymentBankId: String,
    @Column(name = "payment_pass")
    var paymentPass: String? = null,
    @Column(name = "qrc_id")
    var qrcId: String? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
)
