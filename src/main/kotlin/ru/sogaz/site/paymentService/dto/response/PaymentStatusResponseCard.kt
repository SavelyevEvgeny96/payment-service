package ru.sogaz.site.paymentService.dto.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.sogaz.site.paymentService.enums.StatusEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentStatusResponseCard(
    @JsonAlias("result")
    val result: ResultCard,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResultCard(
    val status: StatusEnum,
)
