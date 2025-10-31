package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbCardPaymentStatusResponse(
    @JsonProperty("token")
    val id: String,
    @JsonProperty("result")
    val result: GpbResult?,
    @JsonProperty("portalType")
    val portalType: String?,
    @JsonProperty("src")
    val gpbCardDetails: GpbCardDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbCardDetails(
    val type: String? = null,
    val pan: String? = null,
    val paymentSystem: String? = null,
    val issuerName: String? = null,
)
