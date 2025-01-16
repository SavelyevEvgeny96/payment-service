package ru.sogaz.site.payment_service.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.payment_service.entity.PaymentOperationHistory
@Repository
interface PaymentOperationHistoryRepository: JpaRepository<PaymentOperationHistory,Long> {
}