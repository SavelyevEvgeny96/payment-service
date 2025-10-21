package ru.sogaz.site.paymentService.dto.request

import ru.sogaz.site.paymentService.enums.CurrencyEnum

data class GPBSBPPaymentRequest(
    val account: String?,
    val merchantId: String,
    val templateVersion: String,
    val qrcType: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val paymentPurpose: String?,
    val callbackMerchantNotifications: String,
    val qrTtl: String,
)
