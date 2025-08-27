package ru.sogaz.site.paymentService.dto.request

class AkbCardPaymentRequest(
    val order: OrderDto,
)

data class OrderDto(
    val typeRid: String,
    val amount: Int?,
    val currency: String,
    val description: String,
    val language: String,
    val hppRedirectUrl: String,
    val adviceIfaceAddress: String?,
    val descriptionHtml: String,
    val ridByMerchant: String,
)
