package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GazpromTokenResponse(
    val token: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GazpromCardPaymentResponse(
    val token: String,
    val options: Options,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Options(
    val paymentPageUrl: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GazpromSBPPaymentResponse(
    val data: SBPData = SBPData(),
    val transactionId: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SBPData(
    val qrcId: String = "",
    val payload: String = "",
)
