package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.PrevStatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAkbStatusResponse(
    @JsonProperty("order") val order: OrdersAkb,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdersAkb(
    @JsonProperty("status") val status: String,
    @JsonProperty("prevStatus") val prevStatus: PrevStatusEnum,
)
