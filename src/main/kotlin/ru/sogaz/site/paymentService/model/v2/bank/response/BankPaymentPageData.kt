package ru.sogaz.site.paymentService.model.v2.bank.response

import ru.sogaz.site.paymentService.enums.BankEnum

data class BankPaymentPageData(
    val id: String,
    val bank: BankEnum,
    val paymentPageUrl: String,
)
