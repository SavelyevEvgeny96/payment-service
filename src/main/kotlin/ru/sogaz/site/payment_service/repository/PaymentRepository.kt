package ru.sogaz.site.payment_service.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.payment_service.entity.Payment
@Repository
interface PaymentRepository : JpaRepository<Payment,Long> {
}