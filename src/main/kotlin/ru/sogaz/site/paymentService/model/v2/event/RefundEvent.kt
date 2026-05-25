package ru.sogaz.site.paymentService.model.v2.event

import ru.sogaz.site.paymentService.enums.BankEnum
import java.math.BigDecimal

data class RefundEvent(
    val paymentBankId: String,
    val amount: BigDecimal,
    val bank: BankEnum,
    val type: String,
    val description: String,
)
