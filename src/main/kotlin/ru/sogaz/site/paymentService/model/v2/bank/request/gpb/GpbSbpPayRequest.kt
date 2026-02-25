package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

import ru.sogaz.site.paymentService.enums.CurrencyEnum

data class GpbSbpPayRequest(
    val account: String?,
    val merchantId: String,
    val templateVersion: String,
    val qrcType: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val paymentPurpose: String?,
    val callbackMerchantNotifications: String,
    val qrTtl: String,
    val subscriptionPurpose: String?,
)
