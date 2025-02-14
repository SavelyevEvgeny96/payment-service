package ru.sogaz.site.paymentService.entity

import jakarta.persistence.*

@Entity
@Table(name = "payment_type")
class PaymentType  (
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
var id: String,
@Column(name = "type_id")
var typeId: String,
@Column(name = "type_name")
var typeName: String,
)