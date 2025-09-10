package ru.sogaz.site.paymentService.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.sogaz.site.paymentService.enums.PrevStatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentAkbStatusResponse(
    val status: String,
    val prevStatus: PrevStatusEnum,
)
