package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import java.util.UUID

data class SinglePaymentResult(
    val orderIdRecurrent: UUID?,
    val status: PaymentStatusEnum,
)
