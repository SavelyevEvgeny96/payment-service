package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbSbpPaymentStatusResponse(
    @param:JsonProperty("data")
    val result: List<GpbQrResult>,
)
