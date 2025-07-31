package ru.sogaz.site.paymentService.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "payment_operation_history")
class PaymentOperationHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(cascade = [CascadeType.ALL])
    var action: ActionType,
    @Column(name = "action_date")
    var actionDate: LocalDateTime?,
    @ManyToOne
    @JoinColumn(name = "action_author_id")
    var actionAuthor: ClientSystem?,
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    var order: Order,
)
