package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.sogaz.site.paymentService.enums.BankEnum

@Entity
@Table(name = "banks")
data class Bank(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "bank_id")
    var bankId: BankEnum? = null,
    @Column(name = "bank_name")
    var bankName: String = "",
)
