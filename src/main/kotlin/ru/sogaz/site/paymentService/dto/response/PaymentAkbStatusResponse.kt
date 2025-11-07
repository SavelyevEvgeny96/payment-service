package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAkbStatusResponse(
    @param:JsonProperty("order")
    val order: AkbOrderStatusResponse,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AkbOrderStatusResponse(
    @param:JsonProperty("id")
    val id: String,
    @param:JsonProperty("status")
    val status: AkbPaymentStatusEnum,
    @param:JsonProperty("prevStatus")
    val prevStatus: AkbPaymentStatusEnum?,
)
