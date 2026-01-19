package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbCardDetails(
    val type: String? = null,
    val pan: String? = null,
    val paymentSystem: String? = null,
    val issuerName: String? = null,
    val cardId: String? = null,
    val title: String? = null,
)
