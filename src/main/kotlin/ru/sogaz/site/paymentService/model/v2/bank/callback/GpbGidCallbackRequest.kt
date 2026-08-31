package ru.sogaz.site.paymentService.model.v2.bank.callback

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "Callback о состоянии операции ГПБ ГИД")
data class GpbGidCallbackRequest(
    @field:NotBlank
    val pgaTrxId: String,
    @field:NotBlank
    val state: String,
    @field:NotNull
    @field:Valid
    val result: GpbGidCallbackResult,
)

data class GpbGidCallbackResult(
    @field:NotNull
    val status: GpbGidCallbackResultStatus,
    val rrn: String? = null,
    val extendedCode: String? = null,
    val approvalCode: String? = null,
    val isFullyAuthenticated: Boolean? = null,
    val authorizationTime: String? = null,
    val merchId: String? = null,
    val amount: BigDecimal? = null,
    val merchantTrx: String? = null,
    val accountId: String? = null,
    val params: LinkedHashMap<String, String>? = null,
    val srcType: String? = null,
    @field:Valid
    val card: GpbGidCallbackCard? = null,
)

data class GpbGidCallbackCard(
    val ecoCardId: String? = null,
    val paymentSystem: String? = null,
    val maskedPan: String? = null,
    val addToProfile: Boolean? = null,
    val issuerName: String? = null,
)
