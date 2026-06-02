package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbSbpPayStatus

@JsonIgnoreProperties(ignoreUnknown = true)
data class GpbSbpPayDetailsResponse(
    @param:JsonProperty("data")
    val result: List<GpbSbpResult>,
) {
    fun first(): GpbSbpResult = result.first()
}

data class GpbSbpResult(
    @param:JsonProperty("qrcId")
    val id: String?,
    @param:JsonProperty("status")
    val status: GpbSbpPayStatus,
)
