package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import java.util.Optional
import java.util.UUID

interface PaymentDao {
    fun getPayment(
        traceId: String,
        paymentId: UUID,
    ): Payment?

    fun getPaymentFromBankId(bankId: String): Payment

    fun findByPaymentBankId(paymentId: String): Payment

    fun save(payment: Payment): Payment

    fun findPaymentType(paymentType: PaymentTypeEnum): Optional<PaymentType>
}
