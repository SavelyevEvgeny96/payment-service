package ru.sogaz.site.paymentService.dto.request

data class GPBSBPPaymentRequest(
    val account: String?,
    val merchantId: String,
    val templateVersion: String,
    val qrcType: String,
    val amount: String?,
    val currency: String,
    val paymentPurpose: String?,
    val callbackMerchantNotifications: String,
    val qrTtl: String,
)
