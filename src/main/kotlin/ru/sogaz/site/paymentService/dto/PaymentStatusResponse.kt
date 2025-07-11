package ru.sogaz.site.paymentService.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentStatusResponse(
    val result: Result
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
    val status: String
)