package ru.sogaz.site.paymentService.service.payment

import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.MediaTypeValue
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.bank.integration.BankIntegrationFactoryService
import ru.sogaz.site.qr.generator.client.api.QrCodeControllerApi
import ru.sogaz.site.qr.generator.client.model.QRCodeRequest
import ru.sogaz.site.qr.generator.client.model.ResponseQRCodeData
import java.net.URI

class QRCodeServiceImpl(
    private val qrCodeControllerApi: QrCodeControllerApi,
    private val bankIntegrationFactoryService: BankIntegrationFactoryService,
) : QRCodeService {
    companion object {
        private const val QR_GENERATION_FAILED_LOG_MESSAGE = "При генерации QR кода возникла ошибка: "
        private const val QR_REQUEST_FAILED_LOG_MESSAGE = "При запросе QR кода из банка возникла ошибка: "
    }

    private val logger = loggerFor(javaClass)

    override fun generateFileQR(
        uri: URI,
        size: Int,
    ): FileQR? =
        QRCodeRequest()
            .apply { text = uri.toString() }
            .run(::generateFileQR)

    override fun generateFileQR(qrCodeRequest: QRCodeRequest): FileQR? =
        qrCodeRequest
            .runCatching(::requestQRFromQRGeneratorService)
            .run(::handleGenerationResult)

    private fun requestQRFromQRGeneratorService(qrCodeRequest: QRCodeRequest): FileQR =
        qrCodeRequest
            .run(qrCodeControllerApi::generateQRCode)
            .run(::makeFileQREntity)

    private fun makeFileQREntity(responseQRCodeData: ResponseQRCodeData) =
        FileQR(responseQRCodeData.data!!.qrCode, MediaTypeValue.IMAGE_PNG_VALUE)

    private fun handleGenerationResult(result: Result<FileQR>): FileQR? {
        if (result.isFailure) {
            logger.error(QR_GENERATION_FAILED_LOG_MESSAGE, result.exceptionOrNull())
        }
        return result.getOrNull()
    }

    override fun requestFileQRFromBank(payment: Payment): FileQR? =
        payment.bank
            .run(bankIntegrationFactoryService::getInstanceByBank)
            .runCatching { this.getQRCodeImageData(payment) }
            .run(::handleRequestResult)
            ?.let(::makeFileQREntity)

    private fun makeFileQREntity(gpbQrImageResponse: GPBQRImageResponse) =
        FileQR(gpbQrImageResponse.getQRContent(), gpbQrImageResponse.getQRMediaType())

    private fun handleRequestResult(result: Result<GPBQRImageResponse>): GPBQRImageResponse? {
        if (result.isFailure) {
            logger.error(QR_REQUEST_FAILED_LOG_MESSAGE, result.exceptionOrNull())
        }
        return result.getOrNull()
    }
}
