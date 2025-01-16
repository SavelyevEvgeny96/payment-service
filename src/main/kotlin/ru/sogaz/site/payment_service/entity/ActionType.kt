package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "action_type")
class ActionType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "action_name", nullable = false)
    var actionName: String
)