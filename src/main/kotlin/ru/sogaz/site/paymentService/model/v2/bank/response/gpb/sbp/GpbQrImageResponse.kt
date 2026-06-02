package ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp

data class GpbQrImageResponse(
    val data: QrCoreData,
)

data class QrCoreData(
    val image: QrImageData,
)

data class QrImageData(
    val content: String,
    val mediaType: String,
)
