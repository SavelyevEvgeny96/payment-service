package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @UuidGenerator
    var id: UUID? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    var state: PaymentStatusEnum = PaymentStatusEnum.NEW,
    @ManyToOne
    @JoinColumn(name = "order_id")
    var order: Order? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "bank")
    var bank: BankEnum? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    var type: PaymentTypeEnum? = null,
    @Column(name = "qrc_id")
    var qrcId: String? = null,
    @Column(name = "payment_pass")
    var paymentPass: String? = null,
    @Column(name = "payment_bank_id")
    var paymentBankId: String? = null,
    @Column(name = "cheque_name", length = 20)
    var chequeName: String? = null,
    @Column(name = "payment_page_url")
    var paymentPageUrl: String? = null,
    @Column(name = "payment_started")
    var paymentStarted: LocalDateTime? = null,
    @Column(name = "payment_finished")
    var paymentFinished: LocalDateTime? = null,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "update_date")
    var updateDate: LocalDateTime? = null,
    @Transient
    var urlToReturn: UrlToReturn = UrlToReturn(),
) {
    fun checkIs(
        paymentType: PaymentTypeEnum,
        bankEnum: BankEnum? = null,
    ) = when (bankEnum) {
        null -> this.type == paymentType
        else -> this.type == paymentType && this.bank == bankEnum
    }
}
