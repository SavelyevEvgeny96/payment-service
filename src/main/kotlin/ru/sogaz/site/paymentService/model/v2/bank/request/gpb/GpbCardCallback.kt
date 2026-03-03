package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.paymentService.dto.request.CardParams
import ru.sogaz.site.paymentService.dto.request.PaymentParams

/**
 * Поля мапятся из query параметров,
 * поэтому требуется точное соответствие в наименовании
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class GpbCardCallback(
    val trx_id: String,
    val merchant_trx: String,
    val result_code: Int,
    val extResultCode: Int?,
    val amount: Int,
    val account_id: String?,
    val p: PaymentParams?,
    val card: CardParams?,
    val ts: String,
    val signature: String,
)

data class PaymentParams(
    val rrn: String?,
    val authcode: String?,
    val srcType: String?,
    val maskedPan: String?,
    val paymentSystem: String?,
    val issuerName: String?,
    val isFullyAuthenticated: String?,
    val transmissionDateTime: String?,
)

data class CardParams(
    val id: String?,
    val expiry: String?,
    val recurrent: String?,
    val registered: String?,
)
