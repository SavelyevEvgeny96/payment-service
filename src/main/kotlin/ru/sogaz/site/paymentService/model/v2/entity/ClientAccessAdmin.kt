package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "client_access_admin")
class ClientAccessAdmin(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var clientId: String,
    var description: String?,
    @CreationTimestamp
    @Column(updatable = false)
    var createDate: Instant?,
    @UpdateTimestamp
    var updateDate: Instant?,
)
