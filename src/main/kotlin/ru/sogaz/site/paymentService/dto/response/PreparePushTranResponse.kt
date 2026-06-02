package ru.sogaz.site.paymentService.dto.response

data class PreparePushTranResponse(
    val specificByPm: Map<String, IpsRuData>?,
) {
    fun getQrcPayload(key: String): String? = this.specificByPm?.get(key)?.qrcPayload
}

data class IpsRuData(
    val qrcPayload: String?,
    val afterPayRedirectUrl: String?,
)
