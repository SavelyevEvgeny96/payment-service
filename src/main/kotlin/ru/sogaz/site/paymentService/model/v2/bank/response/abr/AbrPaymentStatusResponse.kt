package ru.sogaz.site.paymentService.model.v2.bank.response.abr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class AbrPaymentStatusResponse(
    @param:JsonProperty("order")
    val order: AbrOrderStatusResponse,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AbrOrderStatusResponse(
    @param:JsonProperty("id")
    val id: String,
    @param:JsonProperty("status")
    val status: AbrPaymentStatus,
    @param:JsonProperty("prevStatus")
    val prevStatus: AbrPaymentStatus?,
)

enum class AbrPaymentStatus(
    @JsonValue val value: String,
) {
    PREPARING("Preparing"),
    WAITPUSHTRAN("WaitPushTran"),
    AUTHORIZED("Authorized"),
    PARTPAID("PartPaid"),
    REFUNDED("Refunded"),
    VOIDED("Voided"),
    DECLINED("Declined"),
    EXPIRED("Expired"),
    REFUSED("Refused"),
    FULLYPAID("FullyPaid"),
    CLOSED("Closed"),
}
