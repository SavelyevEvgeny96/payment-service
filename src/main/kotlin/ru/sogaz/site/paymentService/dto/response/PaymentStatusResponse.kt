package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.sogaz.site.paymentService.enums.StatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentStatusResponse(
    val result: Result,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
    val status: StatusEnum,
)
