package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.PaymentType
import java.util.Optional

interface PaymentTypeDao {
    fun getPaymentType(
        traceId: String,
        type: String,
    ): PaymentType

    fun findByTypeId(typeId: String): Optional<PaymentType>
}
