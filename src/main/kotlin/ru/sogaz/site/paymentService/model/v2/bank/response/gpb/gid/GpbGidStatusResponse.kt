package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid

data class GpbGidStatusResponse(
    val actualTimestamp: Long,
    val pgaTrxId: String,
    val state: String,
    val result: GpbGidStatusResult? = null,
    val card: GpbGidStatusCard? = null,
)

data class GpbGidStatusResult(
    val status: String,
    val rrn: String? = null,
    val extendedCode: String? = null,
    val approvalCode: String? = null,
    val isFullyAuthenticated: String? = null,
    val authorizationTime: String? = null,
    val merchId: String? = null,
    val amount: Long? = null,
    val currency: String? = null,
    val merchantTrx: String? = null,
    val accountId: String? = null,
    val params: Map<String, String>? = null,
    val srcType: String? = null,
)

data class GpbGidStatusCard(
    val paymentSystem: String? = null,
    val maskedPan: String? = null,
    val addToProfile: Boolean? = null,
    val issuerName: String? = null,
)
