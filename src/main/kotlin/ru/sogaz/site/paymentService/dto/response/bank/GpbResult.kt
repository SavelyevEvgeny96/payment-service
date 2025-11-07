package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.StatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbResult(
    @param:JsonProperty("status")
    val status: StatusEnum,
)
