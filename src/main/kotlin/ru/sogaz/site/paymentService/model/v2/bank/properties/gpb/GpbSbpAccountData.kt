package ru.sogaz.site.paymentService.model.v2.bank.properties.gpb

data class GpbSbpAccountData(
    val merchantIdSbpGpb: String,
    val paymentAccount: String,
    val callbackUrlSbp: String,
)
