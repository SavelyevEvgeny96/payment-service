package ru.sogaz.site.paymentService.dto.request

class AkbCardAndSbpPaymentRequest(
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
    val srcSubjectKind: String? = null,
    val expTime: String? = null,
    val remittanceMessage: String? = null,
)
