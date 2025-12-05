package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.CurrencyEnum
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GPBPaymentRequest(
    val state: State?,
    val params: Map<String, String>? = null,
    val merchantId: String,
    @param:JsonProperty("back_url_s")
    val backUrlS: String? = null,
    @param:JsonProperty("back_url_f")
    val backUrlF: String? = null,
    val amount: Int,
    val currency: CurrencyEnum,
    val description: String,
    @param:JsonProperty("3ds2")
    val threeDSTwo: ThreeDSTwo? = null,
    val openApiMirPaySupported: Boolean? = null,
    val addCardAllowed: Boolean? = null,
    val merchantTrx: String,
    @JsonIgnore
    val token: String,
    @JsonIgnore
    val depersonalization: Boolean = false,
    var recurrent: Boolean? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val src: Src? = null,
    val returnUrl: String? = null,
)

data class State(
    val redirect: String,
    @param:JsonProperty("in_progress")
    val inProgress: String,
)

data class Src(
    val type: String,
    val cardId: String?,
)

data class ThreeDSTwo(
    val supported: Boolean,
)
