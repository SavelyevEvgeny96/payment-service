package ru.sogaz.site.paymentService.dto.data

import ru.sogaz.site.paymentService.enums.MediaTypeValue

data class DataOrderPaymentPageInfo(
    val orderId: String,
    val urlPayBank: String,
    val paySbp: PaySbp? = null,
)

data class PaySbp(
    val urlPay: String,
    val fileQR: FileQR,
)

data class FileQR(
    val content: String,
    val mediaType: String,
) {
    constructor(content: String, mediaType: MediaTypeValue) : this(content, mediaType.value)
}
