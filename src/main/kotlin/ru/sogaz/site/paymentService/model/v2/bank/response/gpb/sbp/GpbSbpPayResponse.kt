package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp

data class GpbSbpPayResponse(
    val data: SBPData,
    val transactionId: String,
)

data class SBPData(
    val qrcId: String,
    val payload: String?,
)
