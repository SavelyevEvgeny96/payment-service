package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.CurrencyEnum

data class GPBPaymentRequest(
    val state: State?,
    @JsonIgnore
    val token: String,
    val merchantId: String,
    val orderId: String,
    @JsonProperty("back_url_s")
    val backUrlS: String,
    @JsonProperty("back_url_f")
    val backUrlF: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val description: String,
    @JsonProperty("3ds2")
    val threeDSTwo: ThreeDSTwo,
    val openApiMirPaySupported: Boolean,
)

data class State(
    val redirect: String,
    @JsonProperty("in_progress")
    val inProgress: String,
)

data class ThreeDSTwo(
    val supported: Boolean,
)
