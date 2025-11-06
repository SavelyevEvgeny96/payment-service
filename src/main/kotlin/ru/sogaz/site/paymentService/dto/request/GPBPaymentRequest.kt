package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.CurrencyEnum

data class GPBPaymentRequest(
    val state: State?,
    val merchantId: String,
    val orderId: String,
    @param:JsonProperty("back_url_s")
    val backUrlS: String,
    @param:JsonProperty("back_url_f")
    val backUrlF: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val description: String,
    @param:JsonProperty("3ds2")
    val threeDSTwo: ThreeDSTwo,
    val openApiMirPaySupported: Boolean,
    val addCardAllowed: Boolean,
    @JsonIgnore
    val token: String,
    @JsonIgnore
    val depersonalization: Boolean = false,
)

data class State(
    val redirect: String,
    @param:JsonProperty("in_progress")
    val inProgress: String,
)

data class ThreeDSTwo(
    val supported: Boolean,
)
