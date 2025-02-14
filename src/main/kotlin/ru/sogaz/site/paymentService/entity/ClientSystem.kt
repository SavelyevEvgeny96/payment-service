package ru.sogaz.site.paymentService.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "client_systems")
class ClientSystem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(name = "external_system_code", nullable = false)
    var externalSystemCode: String,
    @Column(name = "external_system_name", nullable = false)
    var externalSystemName: String,
)
{
    // Конструктор по умолчанию нужен для JPA
    constructor() : this(0, "", "")
}
