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
import java.util.UUID

@Entity
@Table(name = "waiting_payments")
data class WaitingPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(name = "bank_id")
    var bank: BankEnum? = null,
    @Column(name = "type_id")
    var type: PaymentTypeEnum? = null,
    @Column(name = "payment_bank_id")
    var paymentBankId: String? = null,
    @Column(name = "qrc_id")
    var qrcId: String? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
)
