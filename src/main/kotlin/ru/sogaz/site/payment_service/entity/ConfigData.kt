package ru.sogaz.site.payment_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "config_data")
class ConfigData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "param_name", nullable = false)
    var paramName: String,

    @Column(name = "param_value", nullable = false)
    var paramValue: String
)