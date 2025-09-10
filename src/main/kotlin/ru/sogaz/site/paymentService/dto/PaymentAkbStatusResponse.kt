package ru.sogaz.site.paymentService.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.PrevStatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAkbStatusResponse @JsonCreator constructor(
    @JsonProperty("status") val status: String,
    @JsonProperty("prevStatus") val prevStatus: PrevStatusEnum
)
