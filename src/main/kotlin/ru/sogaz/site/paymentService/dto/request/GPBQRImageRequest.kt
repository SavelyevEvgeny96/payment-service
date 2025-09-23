package ru.sogaz.site.paymentService.dto.request

data class GPBQRImageRequest(
    val qrcId: String,
    val width: Int = 300,
    val height: Int = 300,
)
