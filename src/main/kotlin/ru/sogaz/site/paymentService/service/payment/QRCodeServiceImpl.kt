package ru.sogaz.site.paymentService.service.payment

import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.MediaTypeValue
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.payment.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest
import ru.sogaz.site.qr.generator.client.model.ResponseQRCodeData
import java.util.Optional

class QRCodeServiceImpl(
    private val qrCodeControllerApi: QrCodeControllerApi,
    private val bankIntegrationFactoryService: BankIntegrationFactoryService,
) : QRCodeService {
    companion object {
        const val GPB_INSTEAD_OF_AKB_FOR_QR = "GPB was selected to generate QR code instead of AKB"
    }

    private val logger = loggerFor(javaClass)

    override fun generateQRCode(
        url: String,
        size: Int,
    ): Optional<FileQR> =
        QRCodeRequest()
            .apply { text = url }
            .run(::generateQRCode)

    override fun generateQRCode(qrCodeRequest: QRCodeRequest): Optional<FileQR> =
        try {
            qrCodeRequest
                .run(qrCodeControllerApi::generateQRCode)
                .run(::makeFileQREntity)
                .run { Optional.of(this) }
        } catch (ex: Exception) {
            logger.error(ex.message)
            Optional.empty<FileQR>()
        }

    override fun requestFromBank(payment: Payment): Optional<FileQR> =
        try {
            resolveBankForQRRequest(payment.bank)
                .run { bankIntegrationFactoryService.getInstanceByBank(this) }
                .run { this.getQRCodeImageData(payment) }
                .run { makeFileQREntity(this) }
                .run { Optional.of(this) }
        } catch (ex: Exception) {
            logger.error(ex.message)
            Optional.empty<FileQR>()
        }

    private fun resolveBankForQRRequest(bank: BankEnum?): BankEnum =
        when (bank) {
            BankEnum.AKB_RUS -> BankEnum.GPB.also { logger.info(GPB_INSTEAD_OF_AKB_FOR_QR) }
            else -> BankEnum.GPB
        }

    private fun makeFileQREntity(responseQRCodeData: ResponseQRCodeData) =
        FileQR(responseQRCodeData.data!!.qrCode, MediaTypeValue.IMAGE_PNG_VALUE)

    private fun makeFileQREntity(gpbQrImageResponse: GPBQRImageResponse) =
        FileQR(gpbQrImageResponse.getQRContent(), gpbQrImageResponse.getQRMediaType())
}
