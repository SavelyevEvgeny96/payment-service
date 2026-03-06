package ru.sogaz.site.paymentService.model.v2.bank.response.gpb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class GpbPayCardResponse(
    val token: String,
    val options: GpbPayCardInfo,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbPayCardInfo(
    val paymentPageUrl: String,
)
