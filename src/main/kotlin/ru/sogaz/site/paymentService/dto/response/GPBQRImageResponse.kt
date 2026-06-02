package ru.sogaz.site.paymentService.dto.response

data class GPBQRImageResponse(
    val data: QRCoreData,
) {
    fun getQRContent(): String = data.image.content

    fun getQRMediaType(): String = data.image.mediaType
}

data class QRCoreData(
    val image: QRImageData,
)

data class QRImageData(
    val content: String,
    val mediaType: String,
)
