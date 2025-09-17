package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.sogaz.site.paymentService.enums.StatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentStatusResponse(
    @JsonAlias("data")
    val result: List<Result>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
    val status: StatusEnum,
)
