package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.sogaz.site.paymentService.enums.StatusEnum

data class PaymentStatusResponseCard(
    val src: Src?,
    val portalType: String?,
    @JsonAlias("data")
    val result: Result,
)

data class Src(
    val type: String?,
    val pan: String?,
    val paymentSystem: String?,
    val issuerName: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Result(
    val status: StatusEnum,
)
