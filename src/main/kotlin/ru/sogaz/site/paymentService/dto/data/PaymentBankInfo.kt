package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum

data class PaymentBankInfo(
    val bank: BankEnum,
    val type: PaymentTypeEnum,
    val depersonalization: Boolean = false,
    val paymentBankId: String,
    val paymentPass: String? = null,
    val qrcId: String? = null,
)
