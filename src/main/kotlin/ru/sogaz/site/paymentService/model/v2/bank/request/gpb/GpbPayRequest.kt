package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.enums.CurrencyEnum

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GpbPayRequest(
    val state: State?,
    val params: Map<String, String>? = null,
    val merchantId: String,
    val amount: Int,
    val currency: CurrencyEnum,
    val description: String,
    @field:JsonProperty("3ds2")
    val threeDSTwo: ThreeDSTwo? = null,
    val openApiMirPaySupported: Boolean? = null,
    val addCardAllowed: Boolean? = null,
    val merchantTrx: String,
    var recurrent: Boolean? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val src: Src? = null,
    @field:JsonProperty("return_url")
    val urlToReturn: String? = null,
    @field:JsonProperty("back_url_s")
    val urlToReturnS: String? = null,
    @field:JsonProperty("back_url_f")
    val urlToReturnF: String? = null,
    var cardRegistration: Boolean? = null,
)

data class State(
    val redirect: String,
    @field:JsonProperty("in_progress")
    val inProgress: String,
)

data class Src(
    val type: String,
    val cardId: String?,
)

data class ThreeDSTwo(
    val supported: Boolean,
)
