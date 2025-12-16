package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.enums.PaymentStatusEnum

data class BankPaymentDetails(
    val id: String,
    val status: PaymentStatusEnum,
    val cardDetails: ClientCardDetails? = null,
    val extendedCode: String?,
)
