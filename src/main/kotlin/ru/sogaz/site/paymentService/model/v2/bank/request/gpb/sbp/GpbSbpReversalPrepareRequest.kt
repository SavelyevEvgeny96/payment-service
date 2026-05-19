package ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp

data class GpbSbpReversalPrepareRequest(
    val transactionId: String,
    val amount: Int,
    val currency: String,
    val comment: String,
    val callbackMerchantNotifications: String,
)

