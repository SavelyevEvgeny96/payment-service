package ru.sogaz.site.paymentService.dto.response

import ru.sogaz.site.paymentService.enums.PaymentStatusEnum

data class ResponseStatusPay(
    val paymentStatus: PaymentStatusEnum,
    val cheque: Boolean,
)
