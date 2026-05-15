package ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp

import ru.sogaz.site.paymentService.enums.CurrencyEnum

class GpbSbpReversalPrepareRequest(
    val transactionId: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val comment: String,
    val callbackMerchantNotifications: String,
)
