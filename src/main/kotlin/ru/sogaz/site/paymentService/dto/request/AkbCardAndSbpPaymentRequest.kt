package ru.sogaz.site.paymentService.dto.request

import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.LanguageEnum
import ru.sogaz.site.paymentService.enums.TypeRidEnum

data class AkbCardAndSbpPaymentRequest(
    val order: OrderDto,
)

data class OrderDto(
    val typeRid: TypeRidEnum,
    val amount: Int,
    val currency: CurrencyEnum,
    val description: String,
    val language: LanguageEnum = LanguageEnum.RU,
    val hppRedirectUrl: String,
    val adviceIfaceAddress: String?,
    val descriptionHtml: String,
    val ridByMerchant: String,
    val srcSubjectKind: String? = null,
    val expTime: String? = null,
    val remittanceMessage: String? = null,
)
