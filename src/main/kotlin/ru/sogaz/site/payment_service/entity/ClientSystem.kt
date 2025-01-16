package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "client_systems")
class ClientSystem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "external_system_code", nullable = false)
    var externalSystemCode: String,

    @Column(name = "external_system_name", nullable = false)
    var externalSystemName: String
)