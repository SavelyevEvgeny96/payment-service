package ru.sogaz.site.paymentService.model.v2.bank.response.gpb

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbCardPayStatus

data class GpbCardPayDetailsResponse(
    @param:JsonProperty("token")
    val id: String,
    @param:JsonProperty("result")
    val result: GpbResult?,
    @param:JsonProperty("portalType")
    val portalType: String?,
    @param:JsonProperty("src")
    val gpbCardDetails: GpbCardDetails? = null,
)

data class GpbResult(
    @param:JsonProperty("status")
    val status: GpbCardPayStatus,
    @param:JsonProperty("extendedCode")
    val extendedCode: String?,
)

data class GpbCardDetails(
    val type: String? = null,
    val pan: String? = null,
    val paymentSystem: String? = null,
    val issuerName: String? = null,
    val cardId: String? = null,
    val title: String? = null,
)
