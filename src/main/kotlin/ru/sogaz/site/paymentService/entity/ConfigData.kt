package ru.sogaz.site.paymentService.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "config_data")
data class ConfigData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "param_name", nullable = false)
    var paramName: String,
    @Column(name = "param_value", nullable = false)
    var paramValue: String,
)
