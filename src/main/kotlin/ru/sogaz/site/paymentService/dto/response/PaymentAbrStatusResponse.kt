package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.AbrPaymentStatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAbrStatusResponse(
    @param:JsonProperty("order")
    val order: AbrOrderStatusResponse,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AbrOrderStatusResponse(
    @param:JsonProperty("id")
    val id: String,
    @param:JsonProperty("status")
    val status: AbrPaymentStatusEnum,
    @param:JsonProperty("prevStatus")
    val prevStatus: AbrPaymentStatusEnum?,
)
