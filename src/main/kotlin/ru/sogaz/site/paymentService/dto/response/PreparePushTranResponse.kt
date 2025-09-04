package ru.sogaz.site.paymentService.dto.response

data class PreparePushTranResponse(
    val specificByPm: Map<String, IpsRuData>?
)

data class IpsRuData(
    val qrcPayload: String?,
    val afterPayRedirectUrl: String?
)