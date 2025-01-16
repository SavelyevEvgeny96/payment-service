package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "order_status")
class OrderStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "state_id", nullable = false)
    var stateId: String,

    @Column(name = "state_name", nullable = false)
    var stateName: String
)