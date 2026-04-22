package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest
import java.net.URI

interface QRCodeService {
    fun generateFileQR(
        uri: URI,
        size: Int = 512,
    ): FileQR?

    fun generateFileQR(qrCodeRequest: QRCodeRequest): FileQR?

    fun requestFileQRFromBank(payment: Payment): FileQR?
}
