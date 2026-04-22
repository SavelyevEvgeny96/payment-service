package ru.sogaz.site.paymentService.model.v2.bank.request.abr

data class AbrSetSrcTokenRequest(
    val token: Map<String, Boolean>,
)

data class AbrPreparePushTranRequest(
    val specificByPm: Map<String, Map<String, String>>,
)
