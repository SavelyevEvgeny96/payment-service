package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid

/**
 * Ответ Газпромбанка после регистрации платежной ссылки оплаты картой в ГИД.
 */
data class GpbGidPayResponse(
    val actualTimestamp: Long? = null,
    val paymentPageUrl: String,
    val pgaTrxId: String,
)
