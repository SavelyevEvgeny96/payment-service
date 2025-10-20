package ru.sogaz.site.paymentService.service

import ru.sogaz.site.paymentService.dto.response.GPBQRImageResponse
import ru.sogaz.site.paymentService.entity.Payment

interface BankIntegrationService {
    fun registerPayment(payment: Payment): Payment

    fun getQRCodeImageData(payment: Payment): GPBQRImageResponse
}
