package ru.sogaz.site.paymentService.dto

import jakarta.validation.constraints.NotNull

data class PaymentRequestWrapper(
   @NotNull
    val payments: List<PaymentRequest>,
)
