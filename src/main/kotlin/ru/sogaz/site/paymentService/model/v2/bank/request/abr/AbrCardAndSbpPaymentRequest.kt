package ru.sogaz.site.paymentService.model.v2.bank.request.abr

import ru.sogaz.site.paymentService.enums.CurrencyEnum
import ru.sogaz.site.paymentService.enums.LanguageEnum
import ru.sogaz.site.paymentService.enums.TypeRidEnum

data class AbrCardAndSbpPaymentRequest(
    val order: AbrOrderDto,
)

data class AbrOrderDto(
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
