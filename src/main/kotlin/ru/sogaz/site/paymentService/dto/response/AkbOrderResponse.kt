package ru.sogaz.site.paymentService.dto.response

data class AkbOrderResponse(
    val order: AkbOrderInfo?,
)

data class AkbOrderInfo(
    val id: Int?,
    val hppUrl: String?,
)
