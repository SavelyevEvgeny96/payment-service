package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import java.util.Optional
import java.util.UUID

interface PaymentDao {
    fun getPaymentFromBankId(bankId: String): Payment

    fun findByPaymentBankId(paymentId: String): Payment

    fun findByPaymentOrderId(orderId: UUID?): Optional<Payment>

    fun save(payment: Payment): Payment

    fun findByStatuses(statuses: List<PaymentStatusEnum>): List<Payment>

    fun batchInsertPayment(payments: List<Payment>): List<UUID>
}
