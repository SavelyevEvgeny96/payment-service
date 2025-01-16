package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "banks")
class Bank(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "bank_id", nullable = false)
    var bankId: String,

    @Column(name = "bank_name", nullable = false)
    var bankName: String
)