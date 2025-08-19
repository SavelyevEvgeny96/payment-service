package ru.sogaz.site.paymentService.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAkbStatusResponse(
    val status: String,
    val prevStatus: String,
)
