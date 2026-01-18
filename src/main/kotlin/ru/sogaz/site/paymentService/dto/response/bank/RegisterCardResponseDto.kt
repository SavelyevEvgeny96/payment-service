package ru.sogaz.site.paymentService.dto.response.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RegisterCardResponseDto(
    val token: String? = null,
    var state: String? = null,
    var result: ResultDto? = null,
    @JsonProperty("src")
    val gpbCardDetails: GpbCardDetails? = null,
    var error: String? = null,
    var side: String? = null,
) {
    data class ResultDto(
        val status: String?,
        var extendedCode: String?,
        val trxId: String?,
        val responseCode: String?,
        val rrn: String?,
        val approvalCode: String?,
        val orderStatus: String?,
        val orderStatusChangedAt: Long?,
    )
}
