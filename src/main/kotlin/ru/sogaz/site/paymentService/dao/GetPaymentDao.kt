package ru.sogaz.site.paymentService.dao

import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import java.util.*

interface GetPaymentDao {
    fun getPayment(
        traceId: String,
        paymentId:Long,
    ): Optional<Payment>
}
