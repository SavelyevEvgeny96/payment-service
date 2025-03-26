package ru.sogaz.site.paymentService.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payment_type")
data class PaymentType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String,
    @Column(name = "type_id")
    var typeId: String,
    @Column(name = "type_name")
    var typeName: String,
) {
    // Конструктор по умолчанию нужен для JPA
    constructor() : this(
        "",
        "",
        "",
    )
}
