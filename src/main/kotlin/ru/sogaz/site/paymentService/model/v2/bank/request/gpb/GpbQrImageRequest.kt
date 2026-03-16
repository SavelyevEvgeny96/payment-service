package ru.sogaz.site.paymentService.model.v2.bank.request.gpb

data class GpbQrImageRequest(
    val qrcId: String,
    val width: Int = 300,
    val height: Int = 300,
)
