package ru.sogaz.site.paymentService.dto.response

data class AbrOrderResponse(
    val order: AbrOrderInfo,
)

data class AbrOrderInfo(
    val id: Int,
    val hppUrl: String,
    val password: String,
)
