package ru.sogaz.site.paymentService.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class GPBQRImageRequest(
    @param:JsonProperty("qrcId")
    val qrId: String,
    val width: Int = 300,
    val height: Int = 300,
)
