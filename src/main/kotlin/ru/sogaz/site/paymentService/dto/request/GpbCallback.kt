package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class GpbCallback(
    @param:JsonProperty("trx_id")
    @get:JsonProperty("trx_id")
    val trx_id: String,
    @param:JsonProperty("merch_id")
    val merch_id: String,
    @param:JsonProperty("result_code")
    @get:JsonProperty("result_code")
    val result_code: Int,
    @param:JsonProperty("ext_result_code")
    val extResultCode: Int?,
    val amount: Int,
    @param:JsonProperty("account_id")
    val accountId: String?,
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
