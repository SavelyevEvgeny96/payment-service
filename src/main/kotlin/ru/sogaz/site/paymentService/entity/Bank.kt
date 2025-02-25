package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "banks")
data class Bank(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "bank_id")
    var bankId: String,
    @Column(name = "bank_name")
    var bankName: String,
)
{
    // Конструктор по умолчанию нужен для JPA
    constructor() : this(0, "", "")
}
