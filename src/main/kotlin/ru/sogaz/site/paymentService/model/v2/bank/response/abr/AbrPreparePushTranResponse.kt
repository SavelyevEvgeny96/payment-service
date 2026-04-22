package ru.sogaz.site.paymentService.model.v2.bank.response.abr

data class AbrPreparePushTranResponse(
    val specificByPm: Map<String, AbrIpsRuData>?,
) {
    fun getQrcPayload(key: String): String? = specificByPm?.get(key)?.qrcPayload
}

data class AbrIpsRuData(
    val qrcPayload: String?,
    val afterPayRedirectUrl: String?,
)
