package ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Запрос Газпромбанка на регистрацию платежной ссылки оплаты картой в ГИД.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GpbGidPayRequest(
    val gid: String,
    val ecoCardId: String,
    val portalId: String,
    val merchantId: String,
    val accountId: String,
    val merchantTrx: String,
    val description: String,
    val currency: String,
    val amount: Int,
    val params: Map<String, String>? = null,
    val backUrlSuccess: String,
    val backUrlFail: String,
    val lang: String,
    val addCardAllowed: Boolean? = null,
)
