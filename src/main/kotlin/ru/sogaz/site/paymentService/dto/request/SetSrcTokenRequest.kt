package ru.sogaz.site.paymentService.dto.request

data class SetSrcTokenRequest(
    val token: Map<String, Boolean>
)

data class PreparePushTranRequest(
    val specificByPm: Map<String, Map<String, String>>

)