package ru.sogaz.site.paymentService.model.v2.bank.response.abr

data class AbrOrderResponse(
    val order: AbrOrderInfo,
)

data class AbrOrderInfo(
    val id: Int,
    val hppUrl: String,
    val password: String,
)
