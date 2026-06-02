package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.entity.Payment

data class PaymentRecurrentRegisterData(
    val payment: Payment,
    val bankResponse: RegisterCardResponseDto?,
)
