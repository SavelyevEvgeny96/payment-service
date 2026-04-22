package ru.sogaz.site.paymentService.model.v2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "history_settings_changes")
class HistorySettingsChange(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var actionType: String,
    var action: String,
    var actionAuthor: String,
    @CreationTimestamp
    @Column(updatable = false)
    var actionDate: Instant?,
)
