package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum

interface PaymentDao {
    fun getPaymentFromBankId(bankId: String): Payment

    fun findByPaymentBankId(paymentId: String): Payment

    fun findByOrder(order: Order): Payment?

    fun save(payment: Payment): Payment

    fun findByStatuses(statuses: List<PaymentStatusEnum>): List<Payment>
}
