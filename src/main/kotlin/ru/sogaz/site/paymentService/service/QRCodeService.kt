package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest

interface QRCodeService {
    fun generatePaySbp(
        url: String,
        size: Int = 512,
    ): PaySbp?

    fun generatePaySbp(qrCodeRequest: QRCodeRequest): PaySbp?

    fun requestFromBank(payment: Payment): PaySbp?
}
