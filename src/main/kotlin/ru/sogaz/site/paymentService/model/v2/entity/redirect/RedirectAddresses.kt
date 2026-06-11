package ru.sogaz.site.paymentService.model.v2.entity.redirect

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
@Table(name = "redirect_addresses")
class RedirectAddresses(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    @Column(name = "url_to_return_s")
    var urlToReturnS: String?,
    @Column(name = "url_to_return_f")
    var urlToReturnF: String?,
    @Column(name = "url_to_return")
    var urlToReturn: String?,
    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    var createDate: Instant?,
)
