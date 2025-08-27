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
    var id: Long = 0,
    @Column(name = "param_name")
    var paramName: String = "",
    @Column(name = "param_value")
    var paramValue: String = "",
    @Column(name = "param_description")
    var paramDescription: String = "",
)
