package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest
import java.util.Optional

interface QRCodeService {
    fun generateQRCode(
        url: String,
        size: Int = 512,
    ): Optional<FileQR>

    fun generateQRCode(qrCodeRequest: QRCodeRequest): Optional<FileQR>

    fun requestFromBank(payment: Payment): Optional<FileQR>
}
