package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbCardPaymentStatusResponse(
    @param:JsonProperty("token")
    val id: String,
    @param:JsonProperty("result")
    val result: GpbResult?,
    @param:JsonProperty("portalType")
    val portalType: String?,
    @param:JsonProperty("src")
    val gpbCardDetails: GpbCardDetails? = null,
)
