package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

import ru.sogaz.site.paymentService.enums.CurrencyEnum

data class GpbRefundParams(
    val amount: String,
    val currency: CurrencyEnum,
    val comment: String,
)
